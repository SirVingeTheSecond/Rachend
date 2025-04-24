package dk.sdu.sem.commonweapon;

import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

/**
 * Utility class for damage application.
 */
// This is a temporary hotfix
public final class DamageUtils {
	private static final Logging LOGGER = Logging.createLogger("DamageUtils", LoggingLevel.DEBUG);

	private DamageUtils() {

	}

	/**
	 * Applies damage to a target entity.
	 *
	 * @param target The entity to damage
	 * @param damage Amount of damage to apply
	 * @return True if damage was applied, false otherwise
	 */
	public static boolean applyDamage(Entity target, float damage) {
		if (target == null) return false;

		StatsComponent stats = target.getComponent(StatsComponent.class);
		if (stats == null) return false;

		float currentHealth = stats.getCurrentHealth();
		stats.setCurrentHealth(currentHealth - damage);

		LOGGER.debug("Applied %.1f damage to %s (Health: %.1f -> %.1f)%n",
			damage, target.getID(), currentHealth, stats.getCurrentHealth());


		return true;
	}
}