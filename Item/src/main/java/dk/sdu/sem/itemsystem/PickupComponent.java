package dk.sdu.sem.itemsystem;

import dk.sdu.sem.collision.ITriggerListener;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.commoninventory.InventoryComponent;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.player.PlayerComponent;

import java.util.logging.Logger;

/**
 * Component for handling pickups when entities enter the trigger area.
 * Uses the collision system's trigger events for detection.
 *<p>
 * REQUIRES: The entity must have a ColliderComponent with isTrigger=true to receive trigger events.
 */
// TODO: Revise the design of this component
// Currently, this component is forced to be included in an implementation module.
// This behavior is ambiguous compared to other components.
public class PickupComponent implements IComponent, ITriggerListener {
	private static final Logger LOGGER = Logger.getLogger(PickupComponent.class.getName());
	private static final boolean DEBUG = true;

	private final Entity itemEntity;
	private final String itemType;
	private final float value;
	private boolean consumed = false;

	/**
	 * Creates a pickup component for item collection.
	 *
	 * @param itemEntity The entity this component is attached to
	 * @param itemType The type of item (e.g., "health", "coin", "weapon")
	 * @param value The value of the item
	 * @throws IllegalStateException if the itemEntity doesn't have a required component
	 */
	public PickupComponent(Entity itemEntity, String itemType, float value) {
		this.itemEntity = itemEntity;
		this.itemType = itemType;
		this.value = value;

		// Validate that required components exist
		validateRequiredComponents();
	}

	/**
	 * Validates that all required components exist on the item entity.
	 *
	 * @throws IllegalStateException if a required component is missing
	 */
	private void validateRequiredComponents() {
		if (itemEntity == null) {
			throw new IllegalArgumentException("Item entity cannot be null");
		}

		ColliderComponent collider = itemEntity.getComponent(ColliderComponent.class);
		if (collider == null) {
			throw new IllegalStateException("PickupComponent requires a ColliderComponent on the item entity to function");
		}

		if (!collider.isTrigger()) {
			LOGGER.warning("ColliderComponent on item entity is not set as a trigger. This component might not work as expected.");
		}
	}

	@Override
	public void onTriggerEnter(Entity other) {
		if (consumed) return; // Prevent double collection

		if (DEBUG) {
			LOGGER.info("Trigger entered between " + itemEntity.getID() + " and " + other.getID());
		}

		// Check if the other entity is a player
		if (!other.hasComponent(PlayerComponent.class)) {
			return; // Only players can pick up items
		}

		// Handle based on item type
		switch (itemType) {
			case "health":
				handleHealthPickup(other);
				break;
			case "coin":
				handleCoinPickup(other);
				break;
			default:
				handleGenericPickup(other); // Should either be modified or throw an exception
				break;
		}
	}

	@Override
	public void onTriggerStay(Entity other) {
		// For pickups, we only handle collection on initial trigger
	}

	@Override
	public void onTriggerExit(Entity other) {
		// Pickups are one-time use, so no need to handle exit
	}

	/**
	 * Handles collection of a health pickup.
	 */
	private void handleHealthPickup(Entity collector) {
		StatsComponent stats = collector.getComponent(StatsComponent.class);
		if (stats == null) return;

		float currentHealth = stats.getCurrentHealth();
		float maxHealth = stats.getMaxHealth();

		// Only heal if not at max health
		if (currentHealth < maxHealth) {
			stats.setCurrentHealth(Math.min(currentHealth + value, maxHealth));

			if (DEBUG) {
				LOGGER.info("Healed " + collector.getID() + " for " + value + " health");
				LOGGER.info("Health increased from " + currentHealth + " to " + stats.getCurrentHealth());
			}

			consumeItem();
		}
	}

	/**
	 * Handles collection of a coin pickup.
	 */
	private void handleCoinPickup(Entity collector) {
		InventoryComponent inventory = collector.getComponent(InventoryComponent.class);
		if (inventory == null) return;

		// Add to inventory
		boolean added = inventory.addItem(itemType, (int)value);

		if (added) {
			if (DEBUG) {
				LOGGER.info("Added " + value + " " + itemType + " to inventory");
			}

			consumeItem();
		}
	}

	/**
	 * Handles collection of a generic pickup.
	 */
	private void handleGenericPickup(Entity collector) {
		InventoryComponent inventory = collector.getComponent(InventoryComponent.class);
		if (inventory == null) return;

		// Add to inventory
		boolean added = inventory.addItem(itemType, (int)value);

		if (added) {
			if (DEBUG) {
				LOGGER.info("Added generic item " + itemType + " to inventory");
			}

			consumeItem();
		}
	}

	/**
	 * Marks the item as consumed and removes it from the scene.
	 */
	private void consumeItem() {
		consumed = true;

		// Remove from scene
		if (itemEntity.getScene() != null) {
			itemEntity.getScene().removeEntity(itemEntity);
			if (DEBUG) LOGGER.info("Removed item from scene");
		}
	}

	/**
	 * Gets the type of this item.
	 */
	public String getItemType() {
		return itemType;
	}

	/**
	 * Gets the value of this item.
	 */
	public float getValue() {
		return value;
	}

	/**
	 * Checks if this item has been consumed.
	 */
	public boolean isConsumed() {
		return consumed;
	}
}