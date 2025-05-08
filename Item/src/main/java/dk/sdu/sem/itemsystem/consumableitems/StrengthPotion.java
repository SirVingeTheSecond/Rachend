package dk.sdu.sem.itemsystem.consumableitems;

import dk.sdu.sem.commonitem.IItem;
import dk.sdu.sem.commonitem.ItemType;
import dk.sdu.sem.commonstats.StatModifier;
import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;

public class StrengthPotion implements IItem {
	private final ItemType itemType = ItemType.ConsumableItem;
	private final String itemName = "Strength_Potion";
	private final String spriteName = "Strength_Potion_img";
	private final float damage = 1f;
	private final float knockback = 0.5f;
	private final float bulletSpeed = 0.5f;
	private final float duration = 15f;

	@Override
	public IItem createInstance() {
		return new StrengthPotion();
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

		stats.addModifier(StatType.DAMAGE, StatModifier.createFlat(itemName,damage,duration));
		stats.addModifier(StatType.BULLET_KNOCKBACK, StatModifier.createPercent(itemName,knockback,duration));
		stats.addModifier(StatType.BULLET_SPEED, StatModifier.createPercent(itemName,bulletSpeed,duration));

		return true;
	}
}
