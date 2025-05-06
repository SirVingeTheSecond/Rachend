package dk.sdu.sem.itemsystem.consumableitems;

import dk.sdu.sem.commonitem.IItem;
import dk.sdu.sem.commonitem.ItemType;
import dk.sdu.sem.commonsystem.Entity;

public class Coin implements IItem {
	private final ItemType itemType = ItemType.ConsumableItem;
	private final String itemName = "Coin";
	private final float value = 1f;

	@Override
	public ItemType getType() {
		return itemType;
	}

	@Override
	public String getName() {
		return itemName;
	}

	@Override
	public void applyEffect(Entity entity) {

	}

	@Override
	public IItem createInstance() {
		return new Coin();
	}
}
