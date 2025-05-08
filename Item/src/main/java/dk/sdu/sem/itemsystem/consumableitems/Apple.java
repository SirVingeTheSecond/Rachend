package dk.sdu.sem.itemsystem.consumableitems;

import dk.sdu.sem.commonitem.IItem;
import dk.sdu.sem.commonitem.ItemType;
import dk.sdu.sem.commonstats.StatModifier;
import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;

public class Apple implements IItem {
	private final ItemType itemType = ItemType.ConsumableItem;
	private final String itemName = "Apple";
	private final String spriteName = "Apple_img";
	private final float armor = 100f;
	private final float speed = 1f;
	private final float duration = 10f;

	@Override
	public IItem createInstance() {
		return new Apple();
	}

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
		StatsComponent stats = entity.getComponent(StatsComponent.class);
		if (stats == null)
			throw new IllegalStateException("Entity does not have StatsComponent");

		stats.addModifier(StatType.ARMOR, StatModifier.createFlat(itemName,armor,duration));
		stats.addModifier(StatType.MOVE_SPEED, StatModifier.createPercent(itemName,speed,duration));

		return true;
	}
}
