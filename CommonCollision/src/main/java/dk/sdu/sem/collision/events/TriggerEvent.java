package dk.sdu.sem.collision.events;

import dk.sdu.sem.commonsystem.Entity;

/**
 * Represents a trigger event between two entities.
 */
public class TriggerEvent {
	private final CollisionEventType type;
	private final Entity entity;
	private final Entity otherEntity;

	/**
	 * Creates a new trigger event.
	 *
	 * @param type The type of trigger event (Enter, Stay, Exit)
	 * @param entity The entity that received the trigger
	 * @param otherEntity The other entity involved in the trigger
	 */
	public TriggerEvent(CollisionEventType type, Entity entity, Entity otherEntity) {
		this.type = type;
		this.entity = entity;
		this.otherEntity = otherEntity;
	}

	/**
	 * Gets the type of trigger event.
	 */
	public CollisionEventType getType() {
		return type;
	}

	/**
	 * Gets the entity that received the trigger.
	 */
	public Entity getEntity() {
		return entity;
	}

	/**
	 * Gets the other entity involved in the trigger.
	 */
	public Entity getOtherEntity() {
		return otherEntity;
	}
}