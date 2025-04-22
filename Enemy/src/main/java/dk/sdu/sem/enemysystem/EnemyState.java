package dk.sdu.sem.enemysystem;

/**
 * Enum to represent the state of the enemy.
 */
public enum EnemyState {
	FOLLOWING,  // Following the player with line of sight
	SEARCHING,  // Moving to last known position
	IDLE        // Stopped, no target
}