package dk.sdu.sem.inventory;

import dk.sdu.sem.commonInventory.IPassiveItem;
import dk.sdu.sem.commonInventory.BaseInventory;

import java.util.ArrayList;

public class PassiveItemBaseInventory extends BaseInventory<IPassiveItem> {
	
	private ArrayList<IPassiveItem> itemInventory = new ArrayList<>();

	/**
	 * adds a passive item to the inventory
	 * @param passiveItem
	 */
	@Override
	public void addItem(IPassiveItem passiveItem) {
		itemInventory.add(passiveItem);
	}

	@Override
	public void removeItem(int id) {
		itemInventory.remove(id);
	}

	/**
	 * removes all passive items from the inventory
	 */
	@Override
	public void removeAllItems() {
		itemInventory.clear();
	}
}