package dk.sdu.sem.commonitem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Interface for creating item entities.
 */
public interface IItemFactory {

	/**
	 * Creates an item entity.
	 *
	 * @param position   The position to place the item
	 * @param type       The type of item to create, given by the ItemType enum
	 * @param name       The name of the item
	 * @param spriteName The name of the sprite to use for the item
	 * @return The created item entity
	 */
	Entity createItem(Vector2D position, ItemType type, String name, String spriteName);
}