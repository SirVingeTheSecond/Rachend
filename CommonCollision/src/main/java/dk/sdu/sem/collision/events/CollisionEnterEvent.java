package dk.sdu.sem.collision.events;

import dk.sdu.sem.collision.data.ContactPoint;
import dk.sdu.sem.commonsystem.Entity;

/**
 * Event fired when two colliders begin touching.
 */
public class CollisionEnterEvent extends CollisionEvent {
	/**
	 * Creates a new collision enter event.
	 *
	 * @param entity The entity receiving the event
	 * @param other The other entity involved in the collision
	 * @param contact The contact point information
	 */
	public CollisionEnterEvent(Entity entity, Entity other, ContactPoint contact) {
		super(entity, other, contact);
	}
}
