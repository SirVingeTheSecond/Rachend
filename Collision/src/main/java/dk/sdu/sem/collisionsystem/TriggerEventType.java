package dk.sdu.sem.collisionsystem;

/**
 * Types of trigger events that can occur.
 */
public enum TriggerEventType {
	ENTER,  // First frame of contact
	STAY,   // Continuing contact
	EXIT    // Contact ended
}