package dk.sdu.sem.commonstats;

import dk.sdu.sem.commonsystem.IComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Manages entity stats by using a HashMap to store only the stats relevant to a specific entity type.
 */
public class StatsComponent implements IComponent {
	public static final String STAT_MAX_HEALTH = "maxHealth";
	public static final String STAT_CURRENT_HEALTH = "currentHealth";
	public static final String STAT_MOVE_SPEED = "moveSpeed";
	public static final String STAT_DAMAGE = "damage";
	public static final String STAT_ATTACK_SPEED = "attackSpeed";
	public static final String STAT_ATTACK_RANGE = "attackRange";

	private final Map<String, Float> stats = new HashMap<>();

	private final Map<String, Float> defaultStats = new HashMap<>();

	private final Map<String, Consumer<Float>> statChangeListeners = new ConcurrentHashMap<>();

	/**
	 * Creates an empty stats component.
	 */
	public StatsComponent() {
		defaultStats.put(STAT_MAX_HEALTH, 100f);
		defaultStats.put(STAT_CURRENT_HEALTH, 100f);
		defaultStats.put(STAT_MOVE_SPEED, 200f);
		defaultStats.put(STAT_DAMAGE, 10f);
		defaultStats.put(STAT_ATTACK_SPEED, 1f);
		defaultStats.put(STAT_ATTACK_RANGE, 50f);
	}

	/**
	 * Gets a stat value, returning the default if not explicitly set.
	 *
	 * @param statKey The stat identifier
	 * @return The stat value or default
	 */
	public float getStat(String statKey) {
		if (stats.containsKey(statKey)) {
			return stats.get(statKey);
		}

		if (defaultStats.containsKey(statKey)) {
			return defaultStats.get(statKey);
		}

		return 0f;
	}

	/**
	 * Sets a stat value.
	 *
	 * @param statKey The stat identifier
	 * @param value The new value
	 */
	public void setStat(String statKey, float value) {
		float oldValue = getStat(statKey);
		stats.put(statKey, value);

		// Notify listeners of the change
		if (statChangeListeners.containsKey(statKey)) {
			statChangeListeners.get(statKey).accept(value);
		}
	}

	/**
	 * Modifies a stat by adding to its current value.
	 *
	 * @param statKey The stat identifier
	 * @param amount  The amount to add (negative to subtract)
	 */
	public void modifyStat(String statKey, float amount) {
		float currentValue = getStat(statKey);
		float newValue = currentValue + amount;
		setStat(statKey, newValue);
	}

	/**
	 * Sets the default value for a stat.
	 *
	 * @param statKey The stat identifier
	 * @param defaultValue The default value
	 */
	public void setDefaultStat(String statKey, float defaultValue) {
		defaultStats.put(statKey, defaultValue);
	}

	/**
	 * Resets a stat to its default value.
	 *
	 * @param statKey The stat identifier
	 */
	public void resetStat(String statKey) {
		if (defaultStats.containsKey(statKey)) {
			setStat(statKey, defaultStats.get(statKey));
		} else {
			stats.remove(statKey);
		}
	}

	/**
	 * Checks if a stat is defined.
	 *
	 * @param statKey The stat identifier
	 * @return True if the stat has a value or default
	 */
	public boolean hasStat(String statKey) {
		return stats.containsKey(statKey) || defaultStats.containsKey(statKey);
	}

	/**
	 * Adds a listener for changes to a specific stat.
	 *
	 * @param statKey The stat to listen for
	 * @param listener The callback to execute when the stat changes
	 */
	public void addStatChangeListener(String statKey, Consumer<Float> listener) {
		statChangeListeners.put(statKey, listener);
	}

	/**
	 * Removes a stat change listener.
	 *
	 * @param statKey The stat that was being listened to
	 */
	public void removeStatChangeListener(String statKey) {
		statChangeListeners.remove(statKey);
	}

	/**
	 * Convenience method to get current health.
	 */
	public float getCurrentHealth() {
		return getStat(STAT_CURRENT_HEALTH);
	}

	/**
	 * Convenience method to set current health.
	 */
	public void setCurrentHealth(float health) {
		float maxHealth = getStat(STAT_MAX_HEALTH);
		// Clamp health between 0 and max health
		setStat(STAT_CURRENT_HEALTH, Math.min(maxHealth, Math.max(0f, health)));
	}

	/**
	 * Convenience method to get max health.
	 */
	public float getMaxHealth() {
		return getStat(STAT_MAX_HEALTH);
	}

	/**
	 * Convenience method to set max health.
	 */
	public void setMaxHealth(float maxHealth) {
		setStat(STAT_MAX_HEALTH, Math.max(1f, maxHealth));

		// Ensure current health doesn't exceed new max
		if (getCurrentHealth() > maxHealth) {
			setCurrentHealth(maxHealth);
		}
	}

	/**
	 * Convenience method to get move speed.
	 */
	public float getMoveSpeed() {
		return getStat(STAT_MOVE_SPEED);
	}

	/**
	 * Convenience method to set move speed.
	 */
	public void setMoveSpeed(float speed) {
		setStat(STAT_MOVE_SPEED, speed);
	}

	/**
	 * Convenience method to get damage.
	 */
	public float getDamage() {
		return getStat(STAT_DAMAGE);
	}

	/**
	 * Convenience method to set damage.
	 */
	public void setDamage(float damage) {
		setStat(STAT_DAMAGE, damage);
	}

	/**
	 * Gets all stats as a map.
	 *
	 * @return A copy of the stats map
	 */
	public Map<String, Float> getAllStats() {
		Map<String, Float> allStats = new HashMap<>(defaultStats);
		allStats.putAll(stats); // Overwrite defaults with actual values where set
		return allStats;
	}
}