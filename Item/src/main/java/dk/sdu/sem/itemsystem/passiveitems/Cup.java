package dk.sdu.sem.itemsystem.passiveitems;

import dk.sdu.sem.commonitem.IItem;
import dk.sdu.sem.commonitem.ItemType;
import dk.sdu.sem.commonstats.StatModifier;
import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;

public class Cup implements IItem {
	private final ItemType itemType = ItemType.PassiveItem;
	private final String itemName = "Cup";
	private final String spriteName = "Cup_img";
	private final float health = 1f;
	private final float atkspeed = 1f;
	private final float bulletsize = 0.3f;
	private final float bulletspeed = 0.3f;
	private final float damage = 1f;
	private final float speed = 0.2f;
	private final float knockback = 0.3f;

	@Override
	public IItem createInstance() {
		return new Cup ();
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
		stats.setCurrentHealth(stats.getMaxHealth());
		stats.addModifier(StatType.ATTACK_SPEED, StatModifier.createPermanentFlat(itemName,atkspeed));
		stats.addModifier(StatType.BULLET_KNOCKBACK, StatModifier.createPermanentPercent(itemName,knockback));
		stats.addModifier(StatType.BULLET_SCALE, StatModifier.createPermanentPercent(itemName,bulletsize));
		stats.addModifier(StatType.BULLET_SPEED, StatModifier.createPermanentPercent(itemName,bulletspeed));
		stats.addModifier(StatType.DAMAGE, StatModifier.createPermanentFlat(itemName,damage));
		stats.addModifier(StatType.MOVE_SPEED, StatModifier.createPermanentPercent(itemName,speed));

		return true;
	}
}