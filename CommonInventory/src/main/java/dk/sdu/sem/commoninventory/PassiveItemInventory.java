package dk.sdu.sem.commonInventory;

import java.util.ArrayList;

public class PassiveItemInventory extends BaseInventory<IPassiveItem> {
	
	private ArrayList<IPassiveItem> itemInventory = new ArrayList<>();

	/**
	 * Runs the items onPickUpMethod
	 * @param item
	 */
	public void onItemPickUp(IPassiveItem item) {
		item.onPickUp();
	}
}