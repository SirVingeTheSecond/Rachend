package dk.sdu.sem.itemsystem.passiveitems;

import dk.sdu.sem.commonitem.IItem;
import dk.sdu.sem.commonitem.ItemType;
import dk.sdu.sem.commonstats.StatModifier;
import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;

public class BiggerBullets implements IItem {
	private final ItemType itemType = ItemType.PassiveItem;
	private final String itemName = "Bigger_Bullets";
	private final String spriteName = "Bigger_Bullets_img";
	private final float bulletsize = 0.3f;

	@Override
	public IItem createInstance() {
		return new BiggerBullets();
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

		stats.addModifier(StatType.BULLET_SCALE, StatModifier.createPermanentPercent(itemName,bulletsize));

		return true;
	}
}