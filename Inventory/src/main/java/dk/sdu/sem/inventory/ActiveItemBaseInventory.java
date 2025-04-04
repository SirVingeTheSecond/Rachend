package dk.sdu.sem.inventory;

import dk.sdu.sem.commonInventory.IActiveItem;
import dk.sdu.sem.commonInventory.BaseInventory;

import java.util.ArrayList;

public class ActiveItemBaseInventory extends BaseInventory<IActiveItem> {

	private final int inventorySize = 1;

	private ArrayList<IActiveItem> itemInventory = new ArrayList<>(inventorySize);

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
	public void addItem(IActiveItem item) {
		itemInventory.add(item);
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