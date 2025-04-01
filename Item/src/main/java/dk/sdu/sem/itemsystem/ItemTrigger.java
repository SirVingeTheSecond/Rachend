package dk.sdu.sem.itemsystem;

import dk.sdu.sem.collision.ITriggerListener;
import dk.sdu.sem.commoninventory.InventoryComponent;
import dk.sdu.sem.commonitem.ItemComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.player.PlayerComponent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Component for handling item collection when players enter the item's trigger.
 * Attach this to item entities to make them collectible.
 */
public class ItemTrigger implements IComponent, ITriggerListener {
	private static final Logger LOGGER = Logger.getLogger(ItemTrigger.class.getName());
	private static final boolean DEBUG = true;

	private final Entity itemEntity;

	public ItemTrigger(Entity itemEntity) {
		this.itemEntity = itemEntity;
	}

	@Override
	public void onTriggerEnter(Entity other) {
		if (DEBUG) {
			LOGGER.info("ItemTrigger.onTriggerEnter called");
			LOGGER.info("Item: " + itemEntity.getID() + ", Other: " + other.getID());
		}

		// Check if the other entity is a player
		boolean isPlayer = other.hasComponent(PlayerComponent.class);
		if (DEBUG) LOGGER.info("Other entity is player: " + isPlayer);

		if (isPlayer) {
			// Check if player has inventory
			InventoryComponent inventory = other.getComponent(InventoryComponent.class);
			if (DEBUG) LOGGER.info("Player has inventory: " + (inventory != null));

			if (inventory == null) return;

			// Get item details
			ItemComponent item = itemEntity.getComponent(ItemComponent.class);
			if (item == null) {
				if (DEBUG) LOGGER.warning("Item entity has no ItemComponent!");
				return;
			}

			// Try to add to player's inventory
			String itemType = item.getType();
			int itemValue = item.getValue();

			if (DEBUG) {
				LOGGER.info("Attempting to add item to inventory: " + itemType + " (value: " + itemValue + ")");
			}

			boolean added = inventory.addItem(itemType, itemValue);

			if (added) {
				if (DEBUG) {
					LOGGER.info("Item successfully added to inventory");
					LOGGER.info("Inventory now contains: " + inventory.getItems());
				}

				// Remove item from scene
				if (itemEntity.getScene() != null) {
					itemEntity.getScene().removeEntity(itemEntity);
					if (DEBUG) LOGGER.info("Item removed from scene");
				}
			} else {
				if (DEBUG) LOGGER.info("Could not add item to inventory (possibly full)");
			}
		}
	}

	@Override
	public void onTriggerStay(Entity other) {
		// Items are collected immediately on enter, so we don't need to do anything here
	}

	@Override
	public void onTriggerExit(Entity other) {
		// No action needed for items
	}
}