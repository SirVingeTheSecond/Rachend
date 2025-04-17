package dk.sdu.sem.collision.components;

import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Data component that stores collision information.
 */
public abstract class ColliderComponent implements IComponent {
	private final Entity entity;
	private ICollisionShape shape;
	private PhysicsLayer layer = PhysicsLayer.DEFAULT;
	private boolean isTrigger;
	private boolean enabled = true;
	private Vector2D offset = new Vector2D(0, 0);

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
	 * Gets the world position of this collider.
	 * This is the entity's position plus the collider's offset.
	 *
	 * @return The world position of the collider
	 */
	public Vector2D getWorldPosition() {
		TransformComponent transform = entity.getComponent(TransformComponent.class);
		if (transform == null) {
			return offset;
		}
		return transform.getPosition().add(offset);
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
}