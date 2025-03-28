package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.Entity;

/**
 * Interface for systems that want to be notified about trigger collisions.
 * This is for internal communication between physics/collision systems.
 */
public interface ITriggerCollisionListener {
	/**
	 * Called when the collision system detects a collision involving a trigger collider.
	 *
	 * @param triggerEntity Entity with the trigger collider
	 * @param otherEntity Entity that collided with the trigger
	 * @param isCollisionStart True if this is the first frame of collision, false if ongoing
	 */
	void onTriggerCollision(Entity triggerEntity, Entity otherEntity, boolean isCollisionStart);

	/**
	 * Called when a previously colliding pair stops colliding.
	 *
	 * @param triggerEntity Entity with the trigger collider
	 * @param otherEntity Entity that exited the trigger
	 */
	void onTriggerCollisionEnd(Entity triggerEntity, Entity otherEntity);
}