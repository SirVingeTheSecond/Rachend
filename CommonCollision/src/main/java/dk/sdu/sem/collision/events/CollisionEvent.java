package dk.sdu.sem.collision.events;

import dk.sdu.sem.collision.data.ContactPoint;
import dk.sdu.sem.commonsystem.Entity;

/**
 * Base class for collision events.
 */
public abstract class CollisionEvent {
	private final Entity entity;
	private final Entity other;
	private final ContactPoint contact;

	/**
	 * Creates a new collision event.
	 *
	 * @param entity The entity receiving the event
	 * @param other The other entity involved in the collision
	 * @param contact The contact point information (may be null)
	 */
	protected CollisionEvent(Entity entity, Entity other, ContactPoint contact) {
		this.entity = entity;
		this.other = other;
		this.contact = contact;
	}

	/**
	 * Gets the entity receiving the event.
	 */
	public Entity getEntity() {
		return entity;
	}

	/**
	 * Gets the other entity involved in the collision.
	 */
	public Entity getOther() {
		return other;
	}

	/**
	 * Gets the contact point information.
	 * May be null for exit events or if not available.
	 */
	public ContactPoint getContact() {
		return contact;
	}
}