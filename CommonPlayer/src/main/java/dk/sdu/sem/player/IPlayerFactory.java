package dk.sdu.sem.player;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Interface for creating player entities.
 */
public interface IPlayerFactory {
	/**
	 * Creates a player entity with default settings.
	 * @return The created player entity
	 */
	Entity create();

	/**
	 * Creates a player entity with custom position, speed, and friction.
	 *
	 * @param position Starting position for the player
	 * @param moveSpeed Movement speed
	 * @param friction Physics friction coefficient
	 * @return The created player entity
	 */
	Entity create(Vector2D position, float moveSpeed, float friction);

	/**
	 * Adds a collider to a player entity if collision functionality is available.
	 *
	 * @param player The player entity
	 * @param colliderRadius The radius of the collider
	 */
	void addColliderIfAvailable(Entity player, float colliderRadius);
}
