package dk.sdu.sem.commonweapon;

import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

/**
 * Utility class for damage application.
 */
// This is a temporary hotfix
public final class WeaponDamage {
	private static final Logging LOGGER = Logging.createLogger("DamageUtils", LoggingLevel.DEBUG);

	private WeaponDamage() {

	}

	/**
	 * Applies damage to a target entity.
	 *
	 * @param target The entity to damage
	 * @param damage Amount of damage to apply
	 * @return True if damage was applied, false otherwise
	 */
	public static boolean applyDamage(Entity target, float damage) {
		if (target == null) {
			return false;
		}

		StatsComponent stats = target.getComponent(StatsComponent.class);
		if (stats == null) {
			return false;
		}

		float currentHealth = stats.getCurrentHealth();
		float armor = stats.getStat(StatType.ARMOR);
		damage -= Math.min(damage * (armor / 100), damage);
		float newHealth = currentHealth - damage;
		stats.setCurrentHealth(newHealth);

		LOGGER.debug("Applied %.1f damage to %s: %.1f -> %.1f health", damage, target.getID(), currentHealth, newHealth);

		return true;
	}
}