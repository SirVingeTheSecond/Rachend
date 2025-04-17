package dk.sdu.sem.collision;

import dk.sdu.sem.collision.events.CollisionEnterEvent;
import dk.sdu.sem.collision.events.CollisionExitEvent;
import dk.sdu.sem.collision.events.CollisionStayEvent;

/**
 * Interface for components that want to receive physical collision events.
 */
public interface ICollisionListener {
	/**
	 * Called when a collision begins.
	 *
	 * @param event Information about the collision
	 */
	void onCollisionEnter(CollisionEnterEvent event);

	/**
	 * Called once per fixed update for every collider that is touching.
	 *
	 * @param event Information about the collision
	 */
	void onCollisionStay(CollisionStayEvent event);

	/**
	 * Called when a collision ends.
	 *
	 * @param event Information about the collision
	 */
	void onCollisionExit(CollisionExitEvent event);
}
