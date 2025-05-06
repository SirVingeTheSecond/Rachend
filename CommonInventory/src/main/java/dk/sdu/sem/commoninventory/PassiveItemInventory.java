package dk.sdu.sem.commoninventory;

import java.util.ArrayList;

public class PassiveItemInventory extends BaseInventory<IItem> {
	
	private ArrayList<IItem> itemInventory = new ArrayList<>();

	/**
	 * Runs the items onPickUpMethod
	 * @param item
	 */
	public void onItemPickUp(IItem item) {
		item.useItem();
	}
}