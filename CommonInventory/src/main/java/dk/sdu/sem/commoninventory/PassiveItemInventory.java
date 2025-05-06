package dk.sdu.sem.commoninventory;

import java.util.ArrayList;
import dk.sdu.sem.commonitem.IItem;

public class PassiveItemInventory extends BaseInventory<IItem> {
	
	private ArrayList<IItem> itemInventory = new ArrayList<>();

	/**
	 * Runs the items onPickUpMethod
	 * @param item
	 */
	/* Commented out until it can be streamlined with new applyEffect
	public void onItemPickUp(IItem item) {
		item.applyEffect();
	} */
}