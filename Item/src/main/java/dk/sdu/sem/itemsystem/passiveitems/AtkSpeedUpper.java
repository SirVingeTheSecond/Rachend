package dk.sdu.sem.itemsystem.passiveitems;

import dk.sdu.sem.commonitem.IItem;
import dk.sdu.sem.commonitem.ItemType;
import dk.sdu.sem.commonstats.StatModifier;
import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;

public class AtkSpeedUpper implements IItem {
	private final ItemType itemType = ItemType.PassiveItem;
	private final String itemName = "Atk_Speed_Upper";
	private final String spriteName = "Atk_Speed_Upper_img";
	private final float atkspeed = 1f;

	@Override
	public IItem createInstance() {
		return new AtkSpeedUpper();
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

		stats.addModifier(StatType.ATTACK_SPEED, StatModifier.createPermanentFlat(itemName,atkspeed));

		return true;
	}
}