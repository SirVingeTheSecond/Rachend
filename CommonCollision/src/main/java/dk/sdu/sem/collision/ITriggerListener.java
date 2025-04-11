package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.Entity;

/**
 * Interface for components that want to receive trigger collision events.
 * Similar to Unity's OnTriggerEnter/Stay/Exit callbacks.
 *
 * To use:
 * 1. Implement this interface on any component that needs to respond to trigger events
 * 2. Ensure the entity has a ColliderComponent with isTrigger=true
 * 3. Implement the callback methods to handle enter, stay, and exit events
 */
public interface ITriggerListener {
	/**
	 * Called when another entity's collider enters this entity's trigger collider.
	 * @param other The entity that entered the trigger
	 */
	void onTriggerEnter(Entity other);

	/**
	 * Called every fixed update while another entity's collider stays within this entity's trigger collider.
	 * @param other The entity that is inside the trigger
	 */
	void onTriggerStay(Entity other);

	/**
	 * Called when another entity's collider exits this entity's trigger collider.
	 * @param other The entity that exited the trigger
	 */
	void onTriggerExit(Entity other);
}