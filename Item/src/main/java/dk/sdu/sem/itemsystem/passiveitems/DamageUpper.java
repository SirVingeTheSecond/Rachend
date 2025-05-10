package dk.sdu.sem.itemsystem.passiveitems;

import dk.sdu.sem.commonitem.IItem;
import dk.sdu.sem.commonitem.ItemType;
import dk.sdu.sem.commonstats.StatModifier;
import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;

public class DamageUpper implements IItem {
	private final ItemType itemType = ItemType.PassiveItem;
	private final String itemName = "Damage_Upper";
	private final String spriteName = "Damage_Upper_img";
	private final float damage = 1f;

	@Override
	public IItem createInstance() {
		return new DamageUpper();
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

		stats.addModifier(StatType.DAMAGE, StatModifier.createPermanentFlat(itemName,damage));

		return true;
	}
}
