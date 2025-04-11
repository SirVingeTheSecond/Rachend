package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.Entity;

/**
 * Interface for components that want to receive trigger collision events.
 *
 * To use:
 * 1. Implement this interface on any component that needs to respond to trigger events
 * 2. Ensure the entity has a ColliderComponent with isTrigger=true
 * 3. Implement the callback methods to handle enter, stay, and exit events
 */
public interface ITriggerListener {
	/**
	 * Called when another collider enters this trigger.
	 *
	 * @param other The entity that entered the trigger
	 */
	void onTriggerEnter(Entity other);

	/**
	 * Called once per fixed update for every collider that is in this trigger.
	 *
	 * @param other The entity that is in the trigger
	 */
	void onTriggerStay(Entity other);

	/**
	 * Called when another collider exits this trigger.
	 *
	 * @param other The entity that exited the trigger
	 */
	void onTriggerExit(Entity other);
}