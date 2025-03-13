package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.IEntity;

public interface ICollider {
	/**
	 * Returns the entity associated with this collider.
	 */
	IEntity getEntity();

	/**
	 * Returns the collision shape for this collider.
	 */
	ICollisionShape getCollisionShape();
}
