package dk.sdu.sem.commonweapon;

import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;

/**
 * Utility class for damage application.
 */
// This is a temporary hotfix
public final class DamageUtils {
	private static final boolean DEBUG = false;

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

		if (DEBUG) {
			System.out.printf("Applied %.1f damage to %s (Health: %.1f -> %.1f)%n",
				damage, target.getID(), currentHealth, stats.getCurrentHealth());
		}

		return true;
	}
}