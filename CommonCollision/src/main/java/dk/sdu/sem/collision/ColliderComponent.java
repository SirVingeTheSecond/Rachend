package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.Objects;

/**
 * Component that adds collision capability to an entity.
 */
public class ColliderComponent implements IComponent, ICollider {
	private final Entity entity;
	private final ICollisionShape shape;
	private final boolean isTrigger;
	private Vector2D offset;

	/**
	 * Creates a circular collider.
	 *
	 * @param entity The entity this collider is attached to
	 * @param offset Offset from entity position
	 * @param radius Radius of the collider
	 */
	public ColliderComponent(Entity entity, Vector2D offset, float radius) {
		this.entity = Objects.requireNonNull(entity, "Entity cannot be null");
		this.offset = offset;
		this.shape = new CircleShape(offset, radius);
		this.isTrigger = false;
	}

	/**
	 * Creates a collider with a custom shape.
	 *
	 * @param entity The entity this collider is attached to
	 * @param shape The collision shape
	 * @param isTrigger True if this collider is a trigger (doesn't block movement)
	 */
	public ColliderComponent(Entity entity, ICollisionShape shape, boolean isTrigger) {
		this.entity = Objects.requireNonNull(entity, "Entity cannot be null");
		this.shape = Objects.requireNonNull(shape, "Shape cannot be null");
		this.isTrigger = isTrigger;
		this.offset = new Vector2D(0, 0);
	}

	@Override
	public Entity getEntity() {
		return entity;
	}

	@Override
	public ICollisionShape getCollisionShape() {
		return shape;
	}

	/**
	 * Checks if this collider is a trigger.
	 * Triggers generate collision events but don't block movement.
	 *
	 * @return True if this is a trigger collider
	 */
	public boolean isTrigger() {
		return isTrigger;
	}

	/**
	 * Gets the offset from the entity position.
	 *
	 * @return The offset vector
	 */
	public Vector2D getOffset() {
		return offset;
	}

	/**
	 * Sets the offset from the entity position.
	 *
	 * @param offset The new offset
	 */
	public void setOffset(Vector2D offset) {
		this.offset = offset;
	}

	@Override
	public String toString() {
		return "ColliderComponent{" +
			"entity=" + entity.getID() +
			", shape=" + shape +
			", isTrigger=" + isTrigger +
			'}';
	}
}