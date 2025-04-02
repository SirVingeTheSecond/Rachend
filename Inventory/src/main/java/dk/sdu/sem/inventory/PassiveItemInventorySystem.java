package dk.sdu.sem.inventory;

import dk.sdu.sem.commoninventory.IPassiveItem;
import dk.sdu.sem.commonsystem.IComponent;

import java.util.ArrayList;

public class PassiveItemInventorySystem implements IComponent {
	
	private ArrayList<IPassiveItem> passiveItemInventory = new ArrayList<>();


	/**
	 * Checks if the player has a certain passive item
	 * @param passiveItem
	 * @return The item, unless the item is not in the list, in which case null is returned.
	 */
	public IPassiveItem getPassiveItem(IPassiveItem passiveItem) {

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
	public void addPassiveItem(IPassiveItem passiveItem) {
		passiveItemInventory.add(passiveItem);
	}

	/**
	 * removes a passive item from the inventory
	 * @param passiveItem
	 */
	public void removePassiveItem(IPassiveItem passiveItem) {
		passiveItemInventory.remove(passiveItem);
	}

	public void removePassiveItem(int id) {
		passiveItemInventory.remove(id);
	}

	/**
	 * removes all passive items from the inventory
	 */
	public void removeAllPassiveItems() {
		passiveItemInventory.clear();
	}
}