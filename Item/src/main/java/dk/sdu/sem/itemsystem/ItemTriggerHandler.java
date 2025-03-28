package dk.sdu.sem.itemsystem;

import dk.sdu.sem.collision.ITriggerEventSPI;
import dk.sdu.sem.collision.ITriggerEventInterests;
import dk.sdu.sem.commoninventory.InventoryComponent;
import dk.sdu.sem.commonitem.ItemComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.player.PlayerComponent;

import java.util.HashSet;
import java.util.Set;

/**
 * Trigger event handler for item pickups.
 * Responds to player-item collision events using the node pattern.
 */
public class ItemTriggerHandler implements ITriggerEventSPI, ITriggerEventInterests {

	@Override
	public Set<Class<? extends IComponent>> getComponentsOfInterest() {
		// This handler is interested in events involving players or items
		Set<Class<? extends IComponent>> interests = new HashSet<>();
		interests.add(PlayerComponent.class);
		interests.add(ItemComponent.class);
		return interests;
	}

	@Override
	public void processTriggerEvent(TriggerEventType eventType, Entity triggerEntity, Entity otherEntity) {
		// Only interested in ENTER events for item pickups
		if (eventType != TriggerEventType.ENTER) {
			return;
		}

		// Find player and item entities
		Entity playerEntity = null;
		Entity itemEntity = null;

		// Check which entity is the player and which is the item
		if (otherEntity.hasComponent(PlayerComponent.class) &&
			triggerEntity.hasComponent(ItemComponent.class)) {
			playerEntity = otherEntity;
			itemEntity = triggerEntity;
		} else if (triggerEntity.hasComponent(PlayerComponent.class) &&
			otherEntity.hasComponent(ItemComponent.class)) {
			playerEntity = triggerEntity;
			itemEntity = otherEntity;
		}

		// If we didn't find a player-item interaction, ignore this event
		if (playerEntity == null || itemEntity == null) {
			return;
		}

		// Ensure player has inventory component
		ensurePlayerHasInventory(playerEntity);

		// Get the item component
		ItemComponent item = itemEntity.getComponent(ItemComponent.class);

		// Get the player's inventory
		InventoryComponent inventory = playerEntity.getComponent(InventoryComponent.class);

		if (item != null && inventory != null) {
			// Try to add the item to the inventory
			boolean added = inventory.addItem(item.getType(), item.getValue());

			if (added) {
				// Success - provide feedback and remove item
				System.out.println("Player picked up " + item.getValue() + " " + item.getType() +
					" (Total: " + inventory.getItemCount(item.getType()) + ")");

				// Remove the item entity from the scene
				itemEntity.getScene().removeEntity(itemEntity);

				// TODO: Play pickup sound here
				// TODO: Show pickup effect here
			} else {
				System.out.println("Inventory full, cannot pick up " + item.getType());
				// TODO: Show "inventory full" message
			}
		}
	}

	/**
	 * Ensures the player entity has an inventory component.
	 */
	private void ensurePlayerHasInventory(Entity playerEntity) {
		if (!playerEntity.hasComponent(ItemComponent.class)) {
			playerEntity.addComponent(new InventoryComponent(30)); // Default capacity
		}
	}
}