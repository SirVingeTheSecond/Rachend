package dk.sdu.sem.collision.events;

import dk.sdu.sem.collision.data.ContactPoint;
import dk.sdu.sem.commonsystem.Entity;

/**
 * Event fired when two colliders stop touching.
 */
public class CollisionExitEvent extends CollisionEvent {
	/**
	 * Creates a new collision exit event.
	 *
	 * @param entity The entity receiving the event
	 * @param other The other entity involved in the collision
	 * @param contact The last known contact point information (may be null)
	 */
	public CollisionExitEvent(Entity entity, Entity other, ContactPoint contact) {
		super(entity, other, contact);
	}
}
