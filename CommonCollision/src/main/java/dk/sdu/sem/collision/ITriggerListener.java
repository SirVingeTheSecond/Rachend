package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.Entity;

/**
 * Interface for components that want to receive trigger collision events.
 * Similar to Unity's OnTriggerEnter/Stay/Exit callbacks.
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