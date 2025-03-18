package dk.sdu.sem.collision;

import dk.sdu.sem.gamesystem.data.Entity;

/**
 * Interface for collision components.
 * Represents an entity's physical presence in the world that can collide with other entities.
 */
public interface ICollider {
	/**
	 * Returns the entity associated with this collider.
	 * This allows collision handlers to know which entity was involved in a collision.
	 *
	 * @return the entity associated with this collider
	 */
	Entity getEntity();

	/**
	 * Returns the collision shape for this collider.
	 * The shape determines the physical boundaries used for collision detection.
	 *
	 * @return the collision shape that defines this collider's boundaries
	 */
	ICollisionShape getCollisionShape();
}