package dk.sdu.sem.itemsystem.consumableitems;

import dk.sdu.sem.commonitem.IItem;
import dk.sdu.sem.commonitem.ItemType;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;

public class HealthPotion implements IItem {
	private final ItemType itemType = ItemType.ConsumableItem;
	private final String itemName = "Health_Potion";
	private final float healing = 1f;

	@Override
	public ItemType getType() {
		return itemType;
	}

	@Override
	public String getName() {
		return itemName;
	}

	@Override
	public boolean applyEffect(Entity entity) {
		StatsComponent stats = entity.getComponent(StatsComponent.class);
		if (stats == null)
			throw new IllegalStateException("Entity does not have StatsComponent");

		float currentHealth = stats.getCurrentHealth();
		float maxHealth = stats.getMaxHealth();

		// Only heal if not at max health
		if (currentHealth < maxHealth) {
			float newHealth = Math.min(currentHealth + healing, maxHealth);
			stats.setCurrentHealth(newHealth);

			return true;
		}
		return false;
	}

	@Override
	public IItem createInstance() {
		return new HealthPotion();
	}
}
