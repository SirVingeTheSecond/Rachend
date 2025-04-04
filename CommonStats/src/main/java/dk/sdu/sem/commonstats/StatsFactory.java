package dk.sdu.sem.commonstats;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.player.PlayerComponent;
import dk.sdu.sem.enemy.EnemyComponent;
import dk.sdu.sem.commonitem.ItemComponent;

/**
 * Factory for creating StatsComponent instances based on entity type.
 */
public class StatsFactory {

	/**
	 * Creates appropriate stats for an entity based on its components.
	 *
	 * @param entity The entity to create stats for
	 * @return The created StatsComponent (also added to the entity)
	 */
	public static StatsComponent createStatsFor(Entity entity) {
		StatsComponent stats = new StatsComponent();
		entity.addComponent(stats);

		// Configure based on entity type
		if (entity.hasComponent(PlayerComponent.class)) {
			configurePlayerStats(stats, entity);
		}
		else if (entity.hasComponent(EnemyComponent.class)) {
			configureEnemyStats(stats, entity);
		}
		else if (entity.hasComponent(ItemComponent.class)) {
			configureItemStats(stats, entity);
		}

		return stats;
	}

	/**
	 * Configures stats for a player entity.
	 */
	private static void configurePlayerStats(StatsComponent stats, Entity player) {
		PlayerComponent playerComp = player.getComponent(PlayerComponent.class);

		// Default player stats
		stats.setDefaultStat(StatsComponent.STAT_MAX_HEALTH, 100f);
		stats.setDefaultStat(StatsComponent.STAT_CURRENT_HEALTH, 100f);
		stats.setDefaultStat(StatsComponent.STAT_DAMAGE, 20f);
		stats.setDefaultStat(StatsComponent.STAT_ATTACK_SPEED, 1.0f);
		stats.setDefaultStat(StatsComponent.STAT_ATTACK_RANGE, 50f);

		// Use the move speed from PlayerComponent if available
		if (playerComp != null) {
			stats.setStat(StatsComponent.STAT_MOVE_SPEED, playerComp.getMoveSpeed());
		} else {
			stats.setDefaultStat(StatsComponent.STAT_MOVE_SPEED, 200f);
		}
	}

	/**
	 * Configures stats for an enemy entity.
	 */
	private static void configureEnemyStats(StatsComponent stats, Entity enemy) {
		EnemyComponent enemyComp = enemy.getComponent(EnemyComponent.class);

		// Default enemy stats
		stats.setDefaultStat(StatsComponent.STAT_MAX_HEALTH, 50f);
		stats.setDefaultStat(StatsComponent.STAT_CURRENT_HEALTH, 50f);
		stats.setDefaultStat(StatsComponent.STAT_DAMAGE, 10f);
		stats.setDefaultStat(StatsComponent.STAT_ATTACK_RANGE, 40f);
		stats.setDefaultStat(StatsComponent.STAT_ATTACK_SPEED, 0.8f);

		// Use the move speed from EnemyComponent if available
		if (enemyComp != null) {
			stats.setStat(StatsComponent.STAT_MOVE_SPEED, enemyComp.getMoveSpeed());
		} else {
			stats.setDefaultStat(StatsComponent.STAT_MOVE_SPEED, 150f);
		}
	}

	/**
	 * Configures stats for an item entity.
	 */
	private static void configureItemStats(StatsComponent stats, Entity item) {
		ItemComponent itemComp = item.getComponent(ItemComponent.class);
		if (itemComp == null) return;

		String itemType = itemComp.getType();

		switch (itemType) {
			case "weapon":
				stats.setStat(StatsComponent.STAT_DAMAGE, itemComp.getValue());
				break;
			case "health_potion":
				stats.setStat("healAmount", itemComp.getValue());
				break;
			case "speed_potion":
				stats.setStat("speedBoost", itemComp.getValue());
				break;
		}
	}

	/**
	 * Creates bullet stats.
	 */
	public static StatsComponent createBulletStats(Entity projectile, float damage) {
		StatsComponent stats = new StatsComponent();
		projectile.addComponent(stats);

		// Projectiles need only damage + speed stats
		stats.setStat(StatsComponent.STAT_DAMAGE, damage);
		stats.setStat("bulletSpeed", 800f);
		stats.setStat("lifetime", 2.0f); // 2 seconds before despawning

		return stats;
	}
}