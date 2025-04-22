package dk.sdu.sem.collision.events;

import dk.sdu.sem.commonsystem.Entity;

/**
 * Base class for trigger events.
 */
public abstract class TriggerEvent {
	private final Entity entity;
	private final Entity other;

	/**
	 * Creates a new trigger event.
	 *
	 * @param entity The entity receiving the event
	 * @param other The other entity involved in the trigger
	 */
	protected TriggerEvent(Entity entity, Entity other) {
		this.entity = entity;
		this.other = other;
	}

	/**
	 * Gets the entity receiving the event.
	 */
	public Entity getEntity() {
		return entity;
	}

	/**
	 * Gets the other entity involved in the trigger.
	 */
	public Entity getOther() {
		return other;
	}
}