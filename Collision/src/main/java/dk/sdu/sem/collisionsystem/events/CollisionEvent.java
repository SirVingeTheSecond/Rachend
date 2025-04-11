package dk.sdu.sem.collisionsystem.events;

import dk.sdu.sem.collision.ContactPoint;
import dk.sdu.sem.commonsystem.Entity;

/**
 * Represents a physical collision event between two entities.
 */
public class CollisionEvent {
	private final CollisionEventType type;
	private final Entity entity;
	private final Entity otherEntity;
	private final ContactPoint contactPoint;

	/**
	 * Creates a new collision event.
	 *
	 * @param type The type of collision event (Enter, Stay, Exit)
	 * @param entity The entity that received the collision
	 * @param otherEntity The other entity involved in the collision
	 * @param contactPoint Contact information (may be null for Exit events)
	 */
	public CollisionEvent(CollisionEventType type, Entity entity, Entity otherEntity, ContactPoint contactPoint) {
		this.type = type;
		this.entity = entity;
		this.otherEntity = otherEntity;
		this.contactPoint = contactPoint;
	}

	/**
	 * Gets the type of collision event.
	 */
	public CollisionEventType getType() {
		return type;
	}

	/**
	 * Gets the entity that received the collision.
	 */
	public Entity getEntity() {
		return entity;
	}

	/**
	 * Gets the other entity involved in the collision.
	 */
	public Entity getOtherEntity() {
		return otherEntity;
	}

	/**
	 * Gets contact information for the collision.
	 * May be null for Exit events.
	 */
	public ContactPoint getContactPoint() {
		return contactPoint;
	}
}