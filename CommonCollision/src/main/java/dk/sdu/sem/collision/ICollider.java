package dk.sdu.sem.collision;

import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.commonsystem.Entity;

/**
 * Interface for objects that can collide.
 */
public interface ICollider {
	/**
	 * Gets the entity this collider is attached to.
	 *
	 * @return The entity
	 */
	Entity getEntity();

	/**
	 * Gets the collision shape for this collider.
	 *
	 * @return The collision shape
	 */
	ICollisionShape getCollisionShape();

	/**
	 * Gets the physics layer of this collider.
	 * Default implementation returns DEFAULT layer.
	 *
	 * @return The physics layer
	 */
	default PhysicsLayer getLayer() {
		return PhysicsLayer.DEFAULT;
	}

	/**
	 * Checks if this is a trigger collider.
	 * Default implementation returns false.
	 *
	 * @return True if this is a trigger collider
	 */
	default boolean isTrigger() {
		return false;
	}
}