package dk.sdu.sem.commonitem;

import dk.sdu.sem.commonsystem.IComponent;

public class ItemDropComponent implements IComponent {
	private final String itemPool;
	private float dropChance;

	/**
	 *
	 * @param pool The item pool which to spawn the item from
	 * @param dropChance Chance of the entity dropping an item. Between 0 and 1
	 */
	public ItemDropComponent(String pool, float dropChance) {
		this.itemPool = pool;
		this.dropChance = dropChance;
	}

	public String getItemPool() {
		return itemPool;
	}

	public float getDropChance() {
		return dropChance;
	}

	public void setDropChance(float dropChance) {
		this.dropChance = dropChance;
	}
}
