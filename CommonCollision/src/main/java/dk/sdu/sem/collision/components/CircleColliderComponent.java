package dk.sdu.sem.collision.components;

import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

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
}