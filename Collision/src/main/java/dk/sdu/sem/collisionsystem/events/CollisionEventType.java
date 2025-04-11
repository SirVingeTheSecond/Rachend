package dk.sdu.sem.collisionsystem.events;

/**
 * Types of collision events that can occur.
 * Applies to both physical collisions and triggers.
 */
public enum CollisionEventType {
	/**
	 * Occurs when two colliders first make contact.
	 */
	ENTER,

	/**
	 * Occurs every fixed update while two colliders are in contact.
	 */
	STAY,

	/**
	 * Occurs when two colliders stop touching.
	 */
	EXIT
}