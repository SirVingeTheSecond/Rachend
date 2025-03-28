package dk.sdu.sem.inventory;

import dk.sdu.sem.commoninventory.IActiveItem;
import dk.sdu.sem.commonsystem.IComponent;

import java.util.ArrayList;

public class ActiveItemInventorySystem implements IComponent {

	private final int inventorySize = 1;
	private ArrayList<IActiveItem> activeActiveItemInventory = new ArrayList<>(inventorySize);

	/**
	 * checks if the player has a certain active item
	 * @param activeItem
	 * @return The given item, if the player has it. Else returns null
	 */
	public IActiveItem getActiveItemInInventory(IActiveItem activeItem) {

		for (IActiveItem i : activeActiveItemInventory) {
			if (i.equals(activeItem)) {
				return i;
			}
		}
		return null;
	}

	/**
	 * Uses an active item if the player has it, and then removes it from the inventory
	 * @param activeItem
	 */
	public void useActiveItem(IActiveItem activeItem) {
		if(getActiveItemInInventory(activeItem) != null) {
			activeItem.useItem();
			removeActiveItem(getActiveItemInInventory(activeItem));
		}
	}

	/**
	 * Adds an item to the players inventory
	 * @param activeItem
	 */
	public void addActiveItem(IActiveItem activeItem) {
		activeActiveItemInventory.add(activeItem);
	}

	/**
	 * Removes an item from the players inventory
	 * @param activeItem
	 */
	public void removeActiveItem(IActiveItem activeItem) {
		activeActiveItemInventory.remove(activeItem);
	}

	/**
	 * Removes an item from the players inventory
	 * @param index
	 */
	public void removeActiveItem(int index) {
		activeActiveItemInventory.remove(index);
	}

	/**
	 * Clears the players inventory
	 */
	public void removeAllActiveItems() {
		activeActiveItemInventory.clear();
	}
}