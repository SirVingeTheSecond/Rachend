package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.Entity;

/**
 * Interface for objects that can collide.
 * Bridges the entity system with collision shapes.
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
}