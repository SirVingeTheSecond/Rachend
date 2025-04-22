package dk.sdu.sem.itemsystem;

import dk.sdu.sem.collision.ITriggerListener;
import dk.sdu.sem.collision.events.TriggerEnterEvent;
import dk.sdu.sem.collision.events.TriggerExitEvent;
import dk.sdu.sem.collision.events.TriggerStayEvent;
import dk.sdu.sem.commoninventory.InventoryComponent;
import dk.sdu.sem.commonitem.PickupComponent;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.player.PlayerComponent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Event handling component for item pickups.
 * Listens for trigger events and processes item collection logic.
 */
public class PickupTriggerListener implements IComponent, ITriggerListener {
	private static final Logger LOGGER = Logger.getLogger(PickupTriggerListener.class.getName());
	private static final boolean DEBUG = false; // Set to true for debugging

	private final Entity itemEntity;

	/**
	 * Creates a new pickup trigger listener.
	 *
	 * @param itemEntity The item entity this listener is attached to
	 */
	public PickupTriggerListener(Entity itemEntity) {
		this.itemEntity = itemEntity;
	}

	@Override
	public void onTriggerEnter(TriggerEnterEvent event) {
		Entity triggerEntity = event.getEntity();
		Entity otherEntity = event.getOther();

		// Make sure we process events for our own entity
		if (triggerEntity != itemEntity) {
			return;
		}

		// Get PickupComponent (required)
		PickupComponent pickup = itemEntity.getComponent(PickupComponent.class);
		if (pickup == null) {
			if (DEBUG) LOGGER.warning("PickupTriggerListener attached to entity without PickupComponent");
			return;
		}

		// Skip if already consumed
		if (pickup.isConsumed()) {
			return;
		}

		// Only players can pick up items
		if (!otherEntity.hasComponent(PlayerComponent.class)) {
			return;
		}

		if (DEBUG) {
			LOGGER.log(Level.INFO, "Pickup triggered between {0} and {1}",
				new Object[]{itemEntity.getID(), otherEntity.getID()});
		}

		// Process pickup based on type
		String itemType = pickup.getItemType();
		float value = pickup.getValue();

		boolean collected = false;

		switch (itemType) {
			case "health":
				collected = handleHealthPickup(otherEntity, value);
				break;
			case "coin":
				collected = handleCoinPickup(otherEntity, itemType, value);
				break;
			default:
				collected = handleGenericPickup(otherEntity, itemType, value);
				break;
		}

		// If successfully collected, consume the item
		if (collected) {
			consumeItem(pickup);
		}
	}

	@Override
	public void onTriggerStay(TriggerStayEvent event) {
		// No processing needed for stay events
	}

	@Override
	public void onTriggerExit(TriggerExitEvent event) {
		// No processing needed for exit events
	}

	/**
	 * Handles collection of a health pickup.
	 *
	 * @param collector The entity collecting the health
	 * @param healAmount The amount of health to restore
	 * @return True if health was collected, false otherwise
	 */
	private boolean handleHealthPickup(Entity collector, float healAmount) {
		StatsComponent stats = collector.getComponent(StatsComponent.class);
		if (stats == null) return false;

		float currentHealth = stats.getCurrentHealth();
		float maxHealth = stats.getMaxHealth();

		// Only heal if not at max health
		if (currentHealth < maxHealth) {
			float newHealth = Math.min(currentHealth + healAmount, maxHealth);
			stats.setCurrentHealth(newHealth);

			if (DEBUG) {
				LOGGER.log(Level.INFO, "Healed {0} for {1} health (from {2} to {3})",
					new Object[]{collector.getID(), healAmount, currentHealth, newHealth});
			}

			return true;
		}

		return false;
	}

	/**
	 * Handles collection of a coin pickup.
	 *
	 * @param collector The entity collecting the coin
	 * @param itemType The type of coin
	 * @param value The value of the coin
	 * @return True if coin was collected, false otherwise
	 */
	private boolean handleCoinPickup(Entity collector, String itemType, float value) {
		InventoryComponent inventory = collector.getComponent(InventoryComponent.class);
		if (inventory == null) return false;

		// Add to inventory, coerce to int
		boolean added = inventory.addItem(itemType, (int)value);

		if (added && DEBUG) {
			LOGGER.log(Level.INFO, "Added {0} {1} to inventory of {2}",
				new Object[]{value, itemType, collector.getID()});
		}

		return added;
	}

	/**
	 * Handles collection of a generic pickup.
	 *
	 * @param collector The entity collecting the item
	 * @param itemType The type of item
	 * @param value The value of the item
	 * @return True if item was collected, false otherwise
	 */
	private boolean handleGenericPickup(Entity collector, String itemType, float value) {
		InventoryComponent inventory = collector.getComponent(InventoryComponent.class);
		if (inventory == null) return false;

		// Add to inventory, coerce to int
		boolean added = inventory.addItem(itemType, (int)value);

		if (added && DEBUG) {
			LOGGER.log(Level.INFO, "Added generic item {0} (value {1}) to inventory of {2}",
				new Object[]{itemType, value, collector.getID()});
		}

		return added;
	}

	/**
	 * Marks the item as consumed and removes it from the scene.
	 *
	 * @param pickup The pickup component to mark as consumed
	 */
	private void consumeItem(PickupComponent pickup) {
		pickup.setConsumed(true);

		// Remove from scene if in one
		if (itemEntity.getScene() != null) {
			itemEntity.getScene().removeEntity(itemEntity);
			if (DEBUG) LOGGER.info("Removed consumed item from scene");
		}
	}
}