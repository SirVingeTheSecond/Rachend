package dk.sdu.sem.itemsystem;

import dk.sdu.sem.collision.ITriggerListener;
import dk.sdu.sem.collision.events.TriggerEnterEvent;
import dk.sdu.sem.collision.events.TriggerExitEvent;
import dk.sdu.sem.collision.events.TriggerStayEvent;
import dk.sdu.sem.commoninventory.InventoryComponent;
import dk.sdu.sem.commonitem.ItemComponent;
import dk.sdu.sem.commonitem.ItemType;
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
		ItemComponent pickup = itemEntity.getComponent(ItemComponent.class);
		if (pickup == null) {
			if (DEBUG) LOGGER.warning("PickupTriggerListener attached to entity without PickupComponent");
			return;
		}

		// Skip if already consumed
		if (pickup.isCollected()) {
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
		ItemType itemType = pickup.getType();
		float value = 1;

		boolean collected = false;
		// ToDo change switch case to work with different item types
		switch (itemType) {
			case PassiveItem:
				collected = handlePassivePickup(otherEntity, pickup);
				break;
			case ConsumableItem:
				collected = handleConsumablePickup(otherEntity, pickup);
				break;
			case ActiveItem:
				collected = handleHealthPickup(otherEntity, value);
				break;
		}

		// If successfully collected, consume the item
		if (collected) {
			collectItem(pickup);
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

	//ToDo add some kind of check or safeguard in case of failure, also implement with inventory
	private boolean handlePassivePickup(Entity collector, ItemComponent item) {

		InventoryComponent inventory = collector.getComponent(InventoryComponent.class);
		if (inventory != null)
			inventory.addItem(item.getName(), 1);

		return item.getItem().applyEffect(collector);
	}

	private boolean handleConsumablePickup (Entity collector, ItemComponent item) {
		return item.getItem().applyEffect(collector);
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

		return true;
	}

	/**
	 * Marks the item as consumed and removes it from the scene.
	 *
	 * @param pickup The pickup component to mark as consumed
	 */
	private void collectItem(ItemComponent pickup) {
		pickup.setCollected(true);

		// Remove from scene if in one
		if (itemEntity.getScene() != null) {
			itemEntity.getScene().removeEntity(itemEntity);
			if (DEBUG) LOGGER.info("Removed consumed item from scene");
		}
	}
}