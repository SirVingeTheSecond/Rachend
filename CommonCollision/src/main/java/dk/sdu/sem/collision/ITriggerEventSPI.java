package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.Entity;

/**
 * Service Provider Interface for handling trigger collision events.
 * Modules can implement this interface to receive trigger events.
 */
public interface ITriggerEventSPI {
	/**
	 * Method called by the collision system when a trigger event occurs.
	 *
	 * @param eventType The type of trigger event (ENTER, STAY, EXIT)
	 * @param triggerEntity The entity with the trigger collider
	 * @param otherEntity The entity that entered/stayed/exited the trigger
	 */
	void processTriggerEvent(TriggerEventType eventType, Entity triggerEntity, Entity otherEntity);

	/**
	 * Types of trigger events, matching Unity's concept.
	 */
	enum TriggerEventType {
		ENTER,  // First frame when entities start overlapping
		STAY,   // Subsequent frames while entities continue to overlap
		EXIT    // First frame after entities stop overlapping
	}
}