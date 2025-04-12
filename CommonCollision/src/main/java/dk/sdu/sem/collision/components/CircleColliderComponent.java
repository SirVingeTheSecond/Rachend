package dk.sdu.sem.collision.components;

import dk.sdu.sem.collision.*;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.Optional;
import java.util.ServiceLoader;

/**
 * A circle-shaped collider component.
 */
public class CircleColliderComponent extends ColliderComponent {
	/**
	 * Creates a new circle collider.
	 *
	 * @param entity The entity this collider is attached to
	 * @param radius The radius of the circle
	 */
	public CircleColliderComponent(Entity entity, float radius) {
		super(entity, new CircleShape(radius));
	}

	/**
	 * Creates a new circle collider with offset.
	 *
	 * @param entity The entity this collider is attached to
	 * @param offset The offset from the entity's position
	 * @param radius The radius of the circle
	 */
	public CircleColliderComponent(Entity entity, Vector2D offset, float radius) {
		super(entity, new CircleShape(radius));
		setOffset(offset);
	}

	/**
	 * Creates a new circle collider with specified layer.
	 *
	 * @param entity The entity this collider is attached to
	 * @param radius The radius of the circle
	 * @param layer The physics layer for this collider
	 */
	public CircleColliderComponent(Entity entity, float radius, PhysicsLayer layer) {
		super(entity, new CircleShape(radius));
		setLayer(layer);
	}

	/**
	 * Creates a new circle collider with full options.
	 *
	 * @param entity The entity this collider is attached to
	 * @param offset The offset from the entity's position
	 * @param radius The radius of the circle
	 * @param isTrigger Whether this collider is a trigger
	 * @param layer The physics layer for this collider
	 */
	public CircleColliderComponent(Entity entity, Vector2D offset, float radius, boolean isTrigger, PhysicsLayer layer) {
		super(entity, new CircleShape(radius));
		setOffset(offset);
		setTrigger(isTrigger);
		setLayer(layer);
	}

	/**
	 * Gets the radius of the circle.
	 */
	public float getRadius() {
		return ((CircleShape)getShape()).getRadius();
	}

	@Override
	public AABB getBounds() {
		Vector2D worldPosition = getWorldPosition();
		float radius = ((CircleShape)getShape()).getRadius();

		return new AABB(
			worldPosition.x() - radius,
			worldPosition.y() - radius,
			worldPosition.x() + radius,
			worldPosition.y() + radius
		);
	}

	@Override
	public ContactPoint collidesWith(ColliderComponent other) {
		Optional<ICollisionSPI> collisionSystem = ServiceLoader.load(ICollisionSPI.class).findFirst();

		if (collisionSystem.isPresent()) {
			return collisionSystem.get().getCollisionInfo(this, other);
		}

		return null;
	}

	@Override
	public RaycastHit raycast(Ray ray, float maxDistance) {
		CircleShape circle = (CircleShape)getShape();
		Vector2D worldPos = getWorldPosition();
		float radius = circle.getRadius();

		// Vector from ray origin to circle center
		Vector2D toCenter = worldPos.subtract(ray.getOrigin());

		// Project toCenter onto ray direction
		float projection = toCenter.dot(ray.getDirection());

		// If projection is negative and the ray origin is outside the circle, no hit
		if (projection < 0 && toCenter.magnitudeSquared() > radius * radius) {
			return RaycastHit.noHit();
		}

		// Calculate squared distance from ray to circle center
		float distSquared = toCenter.magnitudeSquared() - (projection * projection);
		float radiusSquared = radius * radius;

		// If ray passes too far from center, no hit
		if (distSquared > radiusSquared) {
			return RaycastHit.noHit();
		}

		// Calculate distance from projection point to intersection points
		float distToIntersection = (float)Math.sqrt(radiusSquared - distSquared);

		// Calculate first intersection distance (projection - distToIntersection)
		float t = projection - distToIntersection;

		// If first intersection is behind ray origin, use second intersection
		if (t < 0) {
			t = projection + distToIntersection;
			if (t < 0 || t > maxDistance) {
				return RaycastHit.noHit();
			}
		}

		// If first intersection is beyond max distance, no hit
		if (t > maxDistance) {
			return RaycastHit.noHit();
		}

		// Calculate hit point and normal
		Vector2D hitPoint = ray.getOrigin().add(ray.getDirection().scale(t));
		Vector2D hitNormal = hitPoint.subtract(worldPos).normalize();

		return new RaycastHit(true, entity, hitPoint, hitNormal, t, this);
	}

	@Override
	public Vector2D closestPoint(Vector2D point) {
		Vector2D worldPos = getWorldPosition();
		float radius = ((CircleShape)getShape()).getRadius();

		// Calculate direction from center to point
		Vector2D toPoint = point.subtract(worldPos);
		float distance = toPoint.magnitude();

		// If point is inside or on the circle, it's already the closest point
		if (distance <= radius) {
			return point;
		}

		// Otherwise, return point on circle surface in the direction of the point
		return worldPos.add(toPoint.normalize().scale(radius));
	}
}