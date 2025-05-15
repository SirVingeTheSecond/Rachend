package dk.sdu.sem.itemsystem.passiveitems;

import dk.sdu.sem.commonitem.IItem;
import dk.sdu.sem.commonitem.ItemType;
import dk.sdu.sem.commonstats.StatModifier;
import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;

public class RapidFireConv implements IItem {
	private final ItemType itemType = ItemType.PassiveItem;
	private final String itemName = "Rapid_Fire_Conv";
	private final String spriteName = "Rapid_Fire_Conv_img";
	private final float damageMulti = 0.5f;
	private final float knockback = 0.5f;
	private final float bulletspeed = 0.5f;
	private final float atkspeed = 1f;

	@Override
	public IItem createInstance() {
		return new LightBullets();
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

		stats.addModifier(StatType.DAMAGE, StatModifier.createPermanentMultiplicative(itemName,damageMulti));
		stats.addModifier(StatType.BULLET_KNOCKBACK, StatModifier.createPermanentMultiplicative(itemName,knockback));
		stats.addModifier(StatType.BULLET_SPEED, StatModifier.createPermanentPercent(itemName,bulletspeed));
		stats.addModifier(StatType.ATTACK_SPEED, StatModifier.createPermanentPercent(itemName,atkspeed));

		return true;
	}
}
