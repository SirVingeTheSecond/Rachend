package dk.sdu.sem.commoninventory;

import java.util.ArrayList;

public class PassiveItemInventory extends BaseInventory<IItem> {
	
	private ArrayList<IPassiveItem> itemInventory = new ArrayList<>();

	/**
	 * Runs the items onPickUpMethod
	 * @param item
	 */
	public void onItemPickUp(IPassiveItem item) {
		item.useItem();
	}
}