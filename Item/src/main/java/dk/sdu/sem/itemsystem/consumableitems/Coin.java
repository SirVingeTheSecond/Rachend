package dk.sdu.sem.itemsystem.consumableitems;

import dk.sdu.sem.commoninventory.InventoryComponent;
import dk.sdu.sem.commonitem.IItem;
import dk.sdu.sem.commonitem.ItemType;
import dk.sdu.sem.commonsystem.Entity;

public class Coin implements IItem {
	private final ItemType itemType = ItemType.ConsumableItem;
	private final String itemName = "Coin";
	private final String spriteName = "Coin_img";
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
	public String getSpriteName() {
		return spriteName;
	}

	@Override
	public boolean applyEffect(Entity entity) {
		InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
		if (inventory == null)
			throw new IllegalStateException("Entity does not have InventoryComponent");

		inventory.addItem(itemName, (int)value);

		return true;
	}

	@Override
	public IItem createInstance() {
		return new Coin();
	}
}
