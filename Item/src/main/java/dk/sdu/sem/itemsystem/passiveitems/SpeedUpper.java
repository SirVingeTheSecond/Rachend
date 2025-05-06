package dk.sdu.sem.itemsystem.passiveitems;

import dk.sdu.sem.commonitem.IItem;
import dk.sdu.sem.commonitem.ItemType;
import dk.sdu.sem.commonstats.StatModifier;
import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;

public class SpeedUpper implements IItem {
	private final ItemType itemType = ItemType.PassiveItem;
	private final String itemName = "Speed_Upper";
	private final float speed = 0.2f;

	@Override
	public IItem createInstance() {
		return new SpeedUpper();
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
	public void applyEffect(Entity entity) {
		StatsComponent stats = entity.getComponent(StatsComponent.class);
		if (stats == null)
			throw new IllegalStateException("Entity does not have StatsComponent");

		stats.addModifier(StatType.MOVE_SPEED, StatModifier.createPermanentPercent(itemName,speed));
	}

}
