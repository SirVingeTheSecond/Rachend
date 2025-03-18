package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.Entity;

public interface ICollider {
	/**
	 * Returns the entity associated with this collider.
	 */
	Entity getEntity();

	/**
	 * Returns the collision shape for this collider.
	 */
	ICollisionShape getCollisionShape();
}
