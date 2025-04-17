package dk.sdu.sem.collision.events;

import dk.sdu.sem.collision.data.ContactPoint;
import dk.sdu.sem.commonsystem.Entity;

/**
 * Event fired every fixed update while two colliders are touching.
 */
public class CollisionStayEvent extends CollisionEvent {
	/**
	 * Creates a new collision stay event.
	 *
	 * @param entity The entity receiving the event
	 * @param other The other entity involved in the collision
	 * @param contact The contact point information
	 */
	public CollisionStayEvent(Entity entity, Entity other, ContactPoint contact) {
		super(entity, other, contact);
	}
}

