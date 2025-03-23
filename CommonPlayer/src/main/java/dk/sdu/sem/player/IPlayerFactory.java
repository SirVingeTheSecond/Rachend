package dk.sdu.sem.player;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Interface for creating player entities.
 * This interface defines the contract for player factory implementations.
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
	 * Optional method to add a collider to a player entity.
	 * This will be called by implementations only if the Collision module is present.
	 *
	 * @param player The player entity
	 * @param colliderRadius The radius of the collider
	 */
	default void addColliderIfAvailable(Entity player, float colliderRadius) {
		try {
			// Try to create a collider component using reflection
			Class<?> colliderClass = Class.forName("dk.sdu.sem.collision.ColliderComponent");
			Object collider = colliderClass.getConstructor(
				Entity.class,
				Vector2D.class,
				float.class
			).newInstance(
				player,
				new Vector2D(0, 0),
				colliderRadius
			);

			// Add the component
			player.addComponent((dk.sdu.sem.commonsystem.IComponent) collider);
			System.out.println("Added collider to player entity");
		} catch (Exception e) {
			// Collision module not present - player works fine without collider
			System.out.println("No collision support available for player");
		}
	}
}