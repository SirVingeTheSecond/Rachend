package dk.sdu.sem.collision.events;

import dk.sdu.sem.commonsystem.Entity;

/**
 * Event fired when an entity exits a trigger.
 */
public class TriggerExitEvent extends TriggerEvent {
	/**
	 * Creates a new trigger exit event.
	 *
	 * @param entity The entity receiving the event
	 * @param other The other entity involved in the trigger
	 */
	public TriggerExitEvent(Entity entity, Entity other) {
		super(entity, other);
	}
}