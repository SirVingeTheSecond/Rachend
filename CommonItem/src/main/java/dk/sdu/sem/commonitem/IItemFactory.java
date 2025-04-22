package dk.sdu.sem.commonitem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Interface for creating item entities.
 */
public interface IItemFactory {
	/**
	 * Creates a default item entity.
	 * Default implementation typically creates a coin at a default position.
	 *
	 * @return The created item entity
	 */
	Entity create();

	/**
	 * Creates a coin item at the specified position.
	 *
	 * @param position Position to place the coin
	 * @return The created coin entity
	 */
	Entity createCoin(Vector2D position);

	/**
	 * Creates a health potion item at the specified position.
	 *
	 * @param position Position to place the health potion
	 * @return The created health potion entity
	 */
	Entity createHealthPotion(Vector2D position);

	/**
	 * Creates a coin item with a specific value.
	 *
	 * @param position Position to place the coin
	 * @param value Value of the coin
	 * @return The created coin entity
	 */
	Entity createCoin(Vector2D position, float value);

	/**
	 * Creates a health potion with a specific healing value.
	 *
	 * @param position Position to place the health potion
	 * @param healAmount Amount of health to restore
	 * @return The created health potion entity
	 */
	Entity createHealthPotion(Vector2D position, float healAmount);
}