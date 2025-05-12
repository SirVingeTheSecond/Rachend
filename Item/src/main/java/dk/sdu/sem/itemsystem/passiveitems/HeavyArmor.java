package dk.sdu.sem.itemsystem.passiveitems;

import dk.sdu.sem.commonitem.IItem;
import dk.sdu.sem.commonitem.ItemType;
import dk.sdu.sem.commonstats.StatModifier;
import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;

public class HeavyArmor implements IItem {
	private final ItemType itemType = ItemType.PassiveItem;
	private final String itemName = "Heavy_Armor";
	private final String spriteName = "Heavy_Armor_img";
	private final float health = 2f;
	private final float movespeed = 0.8f;

	@Override
	public IItem createInstance() {
		return new HeavyArmor();
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

		stats.addModifier(StatType.MAX_HEALTH, StatModifier.createPermanentFlat(itemName,health));
		stats.setCurrentHealth(stats.getCurrentHealth() + health);
		stats.addModifier(StatType.MOVE_SPEED, StatModifier.createPermanentMultiplicative(itemName,movespeed));

		return true;
	}
}