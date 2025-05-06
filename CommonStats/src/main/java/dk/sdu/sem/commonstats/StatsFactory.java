package dk.sdu.sem.commonstats;

import dk.sdu.sem.commonitem.ItemComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.enemy.EnemyComponent;
import dk.sdu.sem.player.PlayerComponent;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Factory for creating StatsComponent instances based on entity type.
 */
public class StatsFactory {
	
	// Stat configurators for components
	private static final Map<Class<? extends IComponent>, BiConsumer<StatsComponent, Entity>> CONFIGURATORS =
		Map.of(
			PlayerComponent.class, (stats, e) -> configurePlayerStats(stats),
			EnemyComponent.class, (stats, e) -> configureEnemyStats(stats, e),
			ItemComponent.class, (stats, e) -> configureItemStats(stats, e)
		);

	/**
	 * Creates appropriate stats for an entity based on its components.
	 *
	 * @param entity The entity to create stats for
	 * @return The created StatsComponent (also added to the entity)
	 */
	public static StatsComponent createStatsFor(Entity entity) {
		StatsComponent stats = new StatsComponent();
		entity.addComponent(stats);

		CONFIGURATORS.entrySet().stream()
			.filter(entry -> entity.hasComponent(entry.getKey()))
			.findFirst()
			.ifPresent(entry -> entry.getValue().accept(stats, entity));

		return stats;
	}
	
	/**
	 * Configures stats for a player entity.
	 */
	private static void configurePlayerStats(StatsComponent stats) {
		stats.setBaseStat(StatType.MAX_HEALTH, 3);
		stats.setBaseStat(StatType.CURRENT_HEALTH, 3);
		stats.setBaseStat(StatType.MOVE_SPEED, 1000f);
	}

	/**
	 * Configures stats for an enemy entity.
	 */
	private static void configureEnemyStats(StatsComponent stats, Entity enemy) {
		EnemyComponent enemyComp = enemy.getComponent(EnemyComponent.class);

		// Set up base stats for enemy
		stats.setBaseStat(StatType.MAX_HEALTH, 50f);
		stats.setBaseStat(StatType.CURRENT_HEALTH, 50f);
		stats.setBaseStat(StatType.ATTACK_RANGE, 40f);

		stats.addModifier(StatType.ATTACK_SPEED, StatModifier.createPermanentPercent("Enemy", -0.2f));
	}

	/**
	 * Configures stats for an item entity.
	 */
	private static void configureItemStats(StatsComponent stats, Entity item) {
		ItemComponent itemComp = item.getComponent(ItemComponent.class);
		if (itemComp == null) return;

		String itemType = itemComp.getType();
		float value = itemComp.getValue();

		switch (itemType) {
			case "weapon":
				stats.setBaseStat(StatType.DAMAGE, value);
				break;
			case "health_potion":
				stats.setBaseStat(StatType.HEAL_AMOUNT, value);
				break;
			case "speed_potion":
				stats.setBaseStat(StatType.SPEED_BOOST, value);
				break;
		}
	}

	/**
	 * Creates bullet stats.
	 */
	public static StatsComponent createBulletStats(Entity projectile, float damage) {
		StatsComponent stats = new StatsComponent();
		projectile.addComponent(stats);

		// Set only the stats that bullets need
		stats.setBaseStat(StatType.DAMAGE, damage);
		stats.setBaseStat(StatType.BULLET_SPEED, 800f);
		stats.setBaseStat(StatType.LIFETIME, 2.0f); // 2 seconds before despawning

		return stats;
	}

	/**
	 * Creates stats for a pickup item.
	 */
	public static StatsComponent createPickupStats(Entity pickup, String pickupType, float value) {
		StatsComponent stats = new StatsComponent();
		pickup.addComponent(stats);

		// Configure based on pickup type
		switch (pickupType) {
			case "health":
				stats.setBaseStat(StatType.HEAL_AMOUNT, value);
				break;
			case "damage_boost":
				stats.setBaseStat(StatType.DAMAGE, value);
				break;
			case "speed_boost":
				stats.setBaseStat(StatType.SPEED_BOOST, value);
				break;
		}

		return stats;
	}
}