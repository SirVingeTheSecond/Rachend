package dk.sdu.sem.collision.components;

import dk.sdu.sem.collision.ICollider;
import dk.sdu.sem.collision.PhysicsLayer;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.collision.shapes.ICollisionShape;
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
	private PhysicsLayer layer;

	/**
	 * Creates a circular collider.
	 *
	 * @param entity The entity this collider is attached to
	 * @param offset Offset from entity position
	 * @param radius Radius of the collider
	 */
	public ColliderComponent(Entity entity, Vector2D offset, float radius) {
		this(entity, offset, radius, PhysicsLayer.DEFAULT);
	}

	/**
	 * Creates a circular collider with a specific layer.
	 *
	 * @param entity The entity this collider is attached to
	 * @param offset Offset from entity position
	 * @param radius Radius of the collider
	 * @param layer The physics layer for collision filtering
	 */
	public ColliderComponent(Entity entity, Vector2D offset, float radius, PhysicsLayer layer) {
		this.entity = Objects.requireNonNull(entity, "Entity cannot be null");
		this.offset = offset;
		this.shape = new CircleShape(offset, radius);
		this.isTrigger = false;
		this.layer = layer;
	}

	/**
	 * Creates a collider with a custom shape.
	 *
	 * @param entity The entity this collider is attached to
	 * @param shape The collision shape
	 * @param isTrigger True if this collider is a trigger (doesn't block movement)
	 */
	public ColliderComponent(Entity entity, ICollisionShape shape, boolean isTrigger) {
		this(entity, shape, isTrigger, PhysicsLayer.DEFAULT);
	}

	/**
	 * Creates a collider with a custom shape and specific layer.
	 *
	 * @param entity The entity this collider is attached to
	 * @param shape The collision shape
	 * @param isTrigger True if this collider is a trigger (doesn't block movement)
	 * @param layer The physics layer for collision filtering
	 */
	public ColliderComponent(Entity entity, ICollisionShape shape, boolean isTrigger, PhysicsLayer layer) {
		this.entity = Objects.requireNonNull(entity, "Entity cannot be null");
		this.shape = Objects.requireNonNull(shape, "Shape cannot be null");
		this.isTrigger = isTrigger;
		this.offset = new Vector2D(0, 0);
		this.layer = layer;
	}

	/**
	 * Creates a circular collider with trigger option.
	 *
	 * @param entity The entity this collider is attached to
	 * @param offset Offset from entity position
	 * @param radius Radius of the collider
	 * @param isTrigger Whether this collider is a trigger
	 */
	public ColliderComponent(Entity entity, Vector2D offset, float radius, boolean isTrigger) {
		this(entity, offset, radius, isTrigger, PhysicsLayer.DEFAULT);
	}

	/**
	 * Creates a circular collider with trigger option and specific layer.
	 *
	 * @param entity The entity this collider is attached to
	 * @param offset Offset from entity position
	 * @param radius Radius of the collider
	 * @param isTrigger Whether this collider is a trigger
	 * @param layer The physics layer for collision filtering
	 */
	public ColliderComponent(Entity entity, Vector2D offset, float radius, boolean isTrigger, PhysicsLayer layer) {
		this.entity = Objects.requireNonNull(entity, "Entity cannot be null");
		this.offset = offset;
		this.shape = new CircleShape(offset, radius);
		this.isTrigger = isTrigger;
		this.layer = layer;
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
	 * Triggers could generate collision events but doesn't block movement.
	 *
	 * @return True if this is a trigger collider
	 */
	@Override
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

	/**
	 * Gets the physics layer of this collider.
	 *
	 * @return The physics layer
	 */
	@Override
	public PhysicsLayer getLayer() {
		return layer;
	}

	/**
	 * Sets the physics layer of this collider.
	 *
	 * @param layer The new physics layer
	 */
	public void setLayer(PhysicsLayer layer) {
		this.layer = layer;
	}

	@Override
	public String toString() {
		return "ColliderComponent{" +
			"entity=" + entity.getID() +
			", shape=" + shape +
			", isTrigger=" + isTrigger +
			", layer=" + layer +
			'}';
	}
}