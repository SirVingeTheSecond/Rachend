package dk.sdu.sem.collision.events;

import dk.sdu.sem.commonsystem.Entity;

/**
 * Event fired every fixed update while an entity is inside a trigger.
 */
public class TriggerStayEvent extends TriggerEvent {
	/**
	 * Creates a new trigger stay event.
	 *
	 * @param entity The entity receiving the event
	 * @param other The other entity involved in the trigger
	 */
	public TriggerStayEvent(Entity entity, Entity other) {
		super(entity, other);
	}
}