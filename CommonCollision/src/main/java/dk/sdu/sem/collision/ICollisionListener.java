package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.Entity;

/**
 * Interface for components that want to receive physical collision events.
 */
public interface ICollisionListener {
	/**
	 * Called when a collision begins.
	 *
	 * @param collision Information about the collision
	 */
	void onCollisionEnter(Collision collision);

	/**
	 * Called once per fixed update for every collider that is touching.
	 *
	 * @param collision Information about the collision
	 */
	void onCollisionStay(Collision collision);

	/**
	 * Called when a collision ends.
	 *
	 * @param collision Information about the collision
	 */
	void onCollisionExit(Collision collision);
}