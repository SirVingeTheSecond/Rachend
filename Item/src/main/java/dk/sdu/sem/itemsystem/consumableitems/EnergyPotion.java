package dk.sdu.sem.itemsystem.consumableitems;

import dk.sdu.sem.commonitem.IItem;
import dk.sdu.sem.commonitem.ItemType;
import dk.sdu.sem.commonstats.StatModifier;
import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;

public class EnergyPotion implements IItem {
	private final ItemType itemType = ItemType.ConsumableItem;
	private final String itemName = "Energy_Potion";
	private final String spriteName = "Energy_Potion_img";
	private final float speed = 0.3f;
	private final float atkspeed = 0.3f;
	private final float duration = 15f;

	@Override
	public IItem createInstance() {
		return new EnergyPotion();
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

		stats.addModifier(StatType.MOVE_SPEED, StatModifier.createPercent(itemName,speed,duration));
		stats.addModifier(StatType.ATTACK_SPEED, StatModifier.createPercent(itemName,atkspeed,duration));

		return true;
	}
}
