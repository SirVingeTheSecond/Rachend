package dk.sdu.sem.inventory;

import dk.sdu.sem.commonInventory.IItem;
import dk.sdu.sem.commonInventory.IPassiveItem;
import dk.sdu.sem.commonInventory.InventorySystem;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.itemsystem.PassiveItem;

import java.util.ArrayList;

public class PassiveItemInventorySystem extends InventorySystem {
	
	private ArrayList<IPassiveItem> passiveItemInventory = new ArrayList<>();


	/**
	 * Checks if the player has a certain passive item
	 * @param passiveItem
	 * @return The item, unless the item is not in the list, in which case null is returned.
	 */
	@Override
	public IPassiveItem getItemInInventory(IItem passiveItem) {

		for (IPassiveItem i : passiveItemInventory) {
			if (i.equals(passiveItem)) {
				return i;
			}
		}
		return null;
	}

	/**
	 * adds a passive item to the inventory
	 * @param passiveItem
	 */
	@Override
	public void addItem(IItem passiveItem) {
		if(passiveItem.getClass() == PassiveItem.class){
			passiveItemInventory.add((IPassiveItem) passiveItem);
		}
	}

	/**
	 * removes a passive item from the inventory
	 * @param passiveItem
	 */
	@Override
	public void removeItem(IItem passiveItem) {
		passiveItemInventory.remove(passiveItem);
	}

	@Override
	public void removeItem(int id) {
		passiveItemInventory.remove(id);
	}

	/**
	 * removes all passive items from the inventory
	 */
	@Override
	public void removeAllItems() {
		passiveItemInventory.clear();
	}
}