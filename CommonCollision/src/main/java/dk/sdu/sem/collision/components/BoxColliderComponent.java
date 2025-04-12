package dk.sdu.sem.collision.components;

import dk.sdu.sem.collision.*;
import dk.sdu.sem.collision.shapes.BoxShape;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.Optional;
import java.util.ServiceLoader;

/**
 * A box-shaped collider component.
 * Similar to Unity's BoxCollider component.
 */
public class BoxColliderComponent extends ColliderComponent {
	/**
	 * Creates a new box collider.
	 *
	 * @param entity The entity this collider is attached to
	 * @param width The width of the box
	 * @param height The height of the box
	 */
	public BoxColliderComponent(Entity entity, float width, float height) {
		super(entity, new BoxShape(width, height));
	}

	/**
	 * Creates a new box collider with offset.
	 *
	 * @param entity The entity this collider is attached to
	 * @param offset The offset from the entity's position
	 * @param width The width of the box
	 * @param height The height of the box
	 */
	public BoxColliderComponent(Entity entity, Vector2D offset, float width, float height) {
		super(entity, new BoxShape(width, height));
		setOffset(offset);
	}

	/**
	 * Creates a new box collider with specified layer.
	 *
	 * @param entity The entity this collider is attached to
	 * @param width The width of the box
	 * @param height The height of the box
	 * @param layer The physics layer for this collider
	 */
	public BoxColliderComponent(Entity entity, float width, float height, PhysicsLayer layer) {
		super(entity, new BoxShape(width, height));
		setLayer(layer);
	}

	/**
	 * Creates a new box collider with full options.
	 *
	 * @param entity The entity this collider is attached to
	 * @param offset The offset from the entity's position
	 * @param width The width of the box
	 * @param height The height of the box
	 * @param isTrigger Whether this collider is a trigger
	 * @param layer The physics layer for this collider
	 */
	public BoxColliderComponent(Entity entity, Vector2D offset, float width, float height, boolean isTrigger, PhysicsLayer layer) {
		super(entity, new BoxShape(width, height));
		setOffset(offset);
		setTrigger(isTrigger);
		setLayer(layer);
	}

	/**
	 * Gets the width of the box.
	 */
	public float getWidth() {
		return ((BoxShape)getShape()).getWidth();
	}

	/**
	 * Gets the height of the box.
	 */
	public float getHeight() {
		return ((BoxShape)getShape()).getHeight();
	}

	@Override
	public AABB getBounds() {
		Vector2D worldPosition = getWorldPosition();
		BoxShape boxShape = (BoxShape)getShape();
		float width = boxShape.getWidth();
		float height = boxShape.getHeight();

		return new AABB(
			worldPosition.x(),
			worldPosition.y(),
			worldPosition.x() + width,
			worldPosition.y() + height
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
		BoxShape box = (BoxShape)getShape();
		Vector2D worldPos = getWorldPosition();

		// Box bounds
		float minX = worldPos.x();
		float minY = worldPos.y();
		float maxX = worldPos.x() + box.getWidth();
		float maxY = worldPos.y() + box.getHeight();

		// Ray properties
		Vector2D origin = ray.getOrigin();
		Vector2D dir = ray.getDirection();

		// Ray-box intersection using slab method
		float invDirX = Math.abs(dir.x()) > 0.0001f ? 1f / dir.x() : Float.MAX_VALUE;
		float invDirY = Math.abs(dir.y()) > 0.0001f ? 1f / dir.y() : Float.MAX_VALUE;

		float tMinX = (minX - origin.x()) * invDirX;
		float tMaxX = (maxX - origin.x()) * invDirX;
		float tMinY = (minY - origin.y()) * invDirY;
		float tMaxY = (maxY - origin.y()) * invDirY;

		if (invDirX < 0) {
			float temp = tMinX;
			tMinX = tMaxX;
			tMaxX = temp;
		}

		if (invDirY < 0) {
			float temp = tMinY;
			tMinY = tMaxY;
			tMaxY = temp;
		}

		float tMin = Math.max(tMinX, tMinY);
		float tMax = Math.min(tMaxX, tMaxY);

		// No intersection or beyond max distance
		if (tMin > tMax || tMax < 0 || tMin > maxDistance) {
			return RaycastHit.noHit();
		}

		// Calculate hit normal
		Vector2D hitNormal;
		if (Math.abs(tMin - tMinX) < 0.0001f) {
			hitNormal = new Vector2D(invDirX < 0 ? 1 : -1, 0);
		} else {
			hitNormal = new Vector2D(0, invDirY < 0 ? 1 : -1);
		}

		// Calculate hit point
		Vector2D hitPoint = origin.add(ray.getDirection().scale(tMin));

		return new RaycastHit(true, entity, hitPoint, hitNormal, tMin, this);
	}

	@Override
	public Vector2D closestPoint(Vector2D point) {
		Vector2D worldPos = getWorldPosition();
		BoxShape box = (BoxShape)getShape();

		// Calculate the bounds of the box
		float minX = worldPos.x();
		float minY = worldPos.y();
		float maxX = worldPos.x() + box.getWidth();
		float maxY = worldPos.y() + box.getHeight();

		// Clamp the point to the box bounds
		float closestX = Math.max(minX, Math.min(point.x(), maxX));
		float closestY = Math.max(minY, Math.min(point.y(), maxY));

		return new Vector2D(closestX, closestY);
	}
}