package dk.sdu.sem.collision;

public interface ICollisionSPI {

	/**
	 * Registers a collider with the collision system.
	 */
	void registerCollider(ICollider collider);

	/**
	 * Unregisters a collider from the collision system.
	 */
	void unregisterCollider(ICollider collider);

	/**
	 * Processes collisions immediately.
	 * This method is called periodically and directly handles collisions (for now).
	 */
	void processCollisions();
}