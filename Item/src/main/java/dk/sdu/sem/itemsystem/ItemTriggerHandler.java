package dk.sdu.sem.itemsystem;

import dk.sdu.sem.collision.ITriggerEventInterests;
import dk.sdu.sem.collision.ITriggerEventSPI;
import dk.sdu.sem.commoninventory.InventoryComponent;
import dk.sdu.sem.commonitem.ItemComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.player.PlayerComponent;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler for item pickup trigger events.
 * Processes collisions between player entities and item entities.
 */
public class ItemTriggerHandler implements ITriggerEventSPI, ITriggerEventInterests {
	private static final Logger LOGGER = Logger.getLogger(ItemTriggerHandler.class.getName());
	private static final boolean DEBUG = false;

	@Override
	public void processTriggerEvent(TriggerEventType eventType, Entity triggerEntity, Entity otherEntity) {
		// Only process ENTER events (first frame of collision)
		if (eventType != TriggerEventType.ENTER) {
			return;
		}

		// Verify this is an item-player collision
		if (!isItemPlayerCollision(triggerEntity, otherEntity)) {
			return;
		}

		try {
			// Determine which entity is the item and which is the player
			Entity itemEntity, playerEntity;
			if (triggerEntity.hasComponent(ItemComponent.class)) {
				itemEntity = triggerEntity;
				playerEntity = otherEntity;
			} else {
				itemEntity = otherEntity;
				playerEntity = triggerEntity;
			}

			// Process the item pickup
			processItemPickup(itemEntity, playerEntity);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error processing item trigger event: " + e.getMessage(), e);
		}
	}

	/**
	 * Determines if this collision is between an item and a player
	 */
	private boolean isItemPlayerCollision(Entity entityA, Entity entityB) {
		boolean hasItem = entityA.hasComponent(ItemComponent.class) ||
			entityB.hasComponent(ItemComponent.class);

		boolean hasPlayer = entityA.hasComponent(PlayerComponent.class) ||
			entityB.hasComponent(PlayerComponent.class);

		return hasItem && hasPlayer;
	}

	/**
	 * Processes an item pickup, adding it to the player's inventory and removing
	 * it from the scene
	 */
	private void processItemPickup(Entity itemEntity, Entity playerEntity) {
		ItemComponent item = itemEntity.getComponent(ItemComponent.class);
		InventoryComponent inventory = playerEntity.getComponent(InventoryComponent.class);

		if (item == null || inventory == null) {
			LOGGER.warning("Missing required components for item pickup");
			return;
		}

		// Get item information
		String itemType = item.getType();
		int itemValue = item.getValue();

		if (DEBUG) {
			LOGGER.info("Processing item pickup: " + itemType + " (value: " + itemValue + ")");
			LOGGER.info("Item Entity ID: " + itemEntity.getID());
			LOGGER.info("Player Entity ID: " + playerEntity.getID());
		}

		// Add item to inventory
		boolean added = inventory.addItem(itemType, itemValue);

		if (added) {
			if (DEBUG) {
				LOGGER.info("Item added to inventory");
				LOGGER.info("Inventory contents: " + inventory.getItems());
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

	@Override
	public Set<Class<? extends IComponent>> getComponentsOfInterest() {
		// Return the component types we're interested in
		Set<Class<? extends IComponent>> components = new HashSet<>();
		components.add(ItemComponent.class);
		components.add(PlayerComponent.class);
		components.add(InventoryComponent.class);
		return components;
	}
}