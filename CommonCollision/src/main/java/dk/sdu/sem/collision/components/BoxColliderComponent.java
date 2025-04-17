package dk.sdu.sem.collision.components;

import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.shapes.BoxShape;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * A box-shaped collider component.
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
}