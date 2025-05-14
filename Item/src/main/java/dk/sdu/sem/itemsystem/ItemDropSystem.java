package dk.sdu.sem.itemsystem;

import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonsystem.Entity;

import java.util.function.BiConsumer;

public class ItemDropSystem {
	private static final ItemFactory itemFactory = new ItemFactory();

	public static void registerDropNode(ItemDropNode dropNode) {
		dropNode.stats.addStatChangeListener(
			StatType.CURRENT_HEALTH,
			new BiConsumer<>() {
				@Override
				public void accept(Float oldValue, Float newValue) {
					if (newValue > 0)
						return;

					if (Math.random() < dropNode.drop.getDropChance()) {
						Entity item = itemFactory.createItemFromPool(dropNode.transform.getPosition(), dropNode.drop.getItemPool());

						dropNode.getEntity().getScene().addEntity(item);
					}

					dropNode.stats.removeStatChangeListener(StatType.CURRENT_HEALTH, this);
				}
			}
		);
	}
}
