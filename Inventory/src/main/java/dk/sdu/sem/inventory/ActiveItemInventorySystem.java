package dk.sdu.sem.inventory;

import dk.sdu.sem.commonInventory.IActiveItem;
import dk.sdu.sem.commonInventory.IItem;
import dk.sdu.sem.commonInventory.InventorySystem;
import dk.sdu.sem.itemsystem.PassiveItem;

import java.util.ArrayList;

public class ActiveItemInventorySystem extends InventorySystem {

	private final int inventorySize = 1;

	private ArrayList<IActiveItem> itemInventory = new ArrayList<>(inventorySize);

	/**
	 * checks if the player has a certain active item
	 * @return The given item, if the player has it. Else returns null
	 */
	@Override
	public IActiveItem getItemInInventory(IItem item) {

		for (IActiveItem i : itemInventory) {
			if (i.equals(item)) {
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
		if(getItemInInventory(activeItem) != null) {
			activeItem.useItem();
			removeItem(getItemInInventory(activeItem));
		}
	}

	/**
	 * Adds an item to the players inventory
	 * @param item
	 */
	@Override
	public void addItem(IItem item) {
		if(item.getClass() == PassiveItem.class) {
			itemInventory.add((IActiveItem) item);
		}
	}

	/**
	 * Removes an item from the players inventory
	 * @param activeItem
	 */
	@Override
	public void removeItem(IItem activeItem) {
		itemInventory.remove(activeItem);
	}

	/**
	 * Removes an item from the players inventory
	 * @param index
	 */
	@Override
	public void removeItem(int index) {
		itemInventory.remove(index);
	}

	/**
	 * Clears the players inventory
	 */
	@Override
	public void removeAllItems() {
		itemInventory.clear();
	}
}