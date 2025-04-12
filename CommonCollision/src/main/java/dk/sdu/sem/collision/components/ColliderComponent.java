package dk.sdu.sem.collision.components;

import dk.sdu.sem.collision.AABB;
import dk.sdu.sem.collision.ContactPoint;
import dk.sdu.sem.collision.PhysicsLayer;
import dk.sdu.sem.collision.Ray;
import dk.sdu.sem.collision.RaycastHit;
import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonsystem.TransformComponent;

/**
 * Base class for all collider components.
 */
public abstract class ColliderComponent implements IComponent {
	protected final Entity entity;
	protected ICollisionShape shape;
	protected PhysicsLayer layer = PhysicsLayer.DEFAULT;
	protected boolean isTrigger;
	protected boolean enabled = true;
	protected Vector2D offset = new Vector2D(0, 0);

	/**
	 * Creates a new collider with the specified shape.
	 *
	 * @param entity The entity this collider is attached to
	 * @param shape The collision shape
	 */
	protected ColliderComponent(Entity entity, ICollisionShape shape) {
		this.entity = entity;
		this.shape = shape;
	}

	/**
	 * Gets the entity this collider is attached to.
	 */
	public Entity getEntity() {
		return entity;
	}

	/**
	 * Gets the collision shape used by this collider.
	 */
	public ICollisionShape getShape() {
		return shape;
	}

	/**
	 * Sets the collision shape used by this collider.
	 *
	 * @param shape The new collision shape
	 */
	protected void setShape(ICollisionShape shape) {
		this.shape = shape;
	}

	/**
	 * Gets the world position of this collider (entity position + offset).
	 */
	public Vector2D getWorldPosition() {
		TransformComponent transform = entity.getComponent(TransformComponent.class);
		if (transform == null) {
			return offset;
		}
		return transform.getPosition().add(offset);
	}

	/**
	 * Checks if this is a trigger collider.
	 * Triggers generate events but don't cause physical collision responses.
	 */
	public boolean isTrigger() {
		return isTrigger;
	}

	/**
	 * Sets whether this is a trigger collider.
	 */
	public void setTrigger(boolean trigger) {
		isTrigger = trigger;
	}

	/**
	 * Gets the physics layer this collider belongs to.
	 */
	public PhysicsLayer getLayer() {
		return layer;
	}

	/**
	 * Sets the physics layer this collider belongs to.
	 */
	public void setLayer(PhysicsLayer layer) {
		this.layer = layer;
	}

	/**
	 * Checks if this collider is enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets whether this collider is enabled.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Gets the offset from the entity's position.
	 */
	public Vector2D getOffset() {
		return offset;
	}

	/**
	 * Sets the offset from the entity's position.
	 */
	public void setOffset(Vector2D offset) {
		this.offset = offset;
	}

	/**
	 * Tests collision with another collider.
	 * This is the core polymorphic method that handles collision between different types.
	 *
	 * @param other The other collider to test against
	 * @return ContactPoint if collision detected, null otherwise
	 */
	public abstract ContactPoint collidesWith(ColliderComponent other);

	/**
	 * Tests if a ray intersects this collider.
	 *
	 * @param ray The ray to test
	 * @param maxDistance Maximum distance to check
	 * @return RaycastHit with intersection details, or null if no intersection
	 */
	public abstract RaycastHit raycast(Ray ray, float maxDistance);

	/**
	 * Gets the closest point on this collider to a specified position.
	 *
	 * @param point The position to find the closest point to
	 * @return The closest point on the collider surface
	 */
	public abstract Vector2D closestPoint(Vector2D point);

	/**
	 * Gets bounds for this collider for broadphase collision detection.
	 *
	 * @return Axis-aligned bounding box covering this collider
	 */
	public abstract AABB getBounds();

	/**
	 * Gets an appropriate debug shape for visualization.
	 */
	public ICollisionShape getDebugShape() {
		return getShape();
	}
}