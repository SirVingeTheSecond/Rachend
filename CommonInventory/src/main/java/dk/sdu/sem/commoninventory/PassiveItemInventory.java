package dk.sdu.sem.commoninventory;

import java.util.ArrayList;
import dk.sdu.sem.commonitem.IItem;

public class PassiveItemInventory extends BaseInventory<IItem> {
	
	private ArrayList<IItem> itemInventory = new ArrayList<>();

	/**
	 * Runs the items onPickUpMethod
	 * @param item
	 */
	public void onItemPickUp(IItem item) {
		item.applyEffect();
	}
}