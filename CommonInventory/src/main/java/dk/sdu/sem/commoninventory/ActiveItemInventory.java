package dk.sdu.sem.commoninventory;

import java.util.ArrayList;

public class ActiveItemInventory extends BaseInventory<IActiveItem> {

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

	public int getInventorySize(){
		return inventorySize;
	}
}