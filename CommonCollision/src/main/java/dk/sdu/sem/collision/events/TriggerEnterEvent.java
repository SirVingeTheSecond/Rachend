package dk.sdu.sem.collision.events;

import dk.sdu.sem.commonsystem.Entity;

/**
 * Event fired when an entity enters a trigger.
 */
public class TriggerEnterEvent extends TriggerEvent {
	/**
	 * Creates a new trigger enter event.
	 *
	 * @param entity The entity receiving the event
	 * @param other The other entity involved in the trigger
	 */
	public TriggerEnterEvent(Entity entity, Entity other) {
		super(entity, other);
	}
}