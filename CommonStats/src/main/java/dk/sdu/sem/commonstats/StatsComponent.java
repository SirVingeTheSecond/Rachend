package dk.sdu.sem.commonstats;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.gamesystem.Time;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Manages entity stats using an enum-based approach for type safety.
 * Supports base values, modifier stacks, and stat dependencies.
 */
public class StatsComponent implements IComponent {
	// Base values for each stat
	private final Map<StatType, Float> baseStats = new EnumMap<>(StatType.class);

	// Default values for each stat
	private final Map<StatType, Float> defaultStats = new EnumMap<>(StatType.class);

	// Modifiers for each stat (stacking)
	private final Map<StatType, List<StatModifier>> statModifiers = new EnumMap<>(StatType.class);

	// Stat change listeners
	private final Map<StatType, Consumer<Float>> statChangeListeners = new ConcurrentHashMap<>();

	// Cached computed values (for performance)
	private final Map<StatType, Float> cachedValues = new EnumMap<>(StatType.class);
	private boolean cacheDirty = true;

	/**
	 * Creates a new stats component with default values.
	 */
	public StatsComponent() {
		// Set up default stats
		setDefaultStat(StatType.MAX_HEALTH, 100f);
		setDefaultStat(StatType.CURRENT_HEALTH, 100f);
		setDefaultStat(StatType.MOVE_SPEED, 200f);
		setDefaultStat(StatType.DAMAGE, 10f);
		setDefaultStat(StatType.ATTACK_SPEED, 1f);
		setDefaultStat(StatType.ATTACK_RANGE, 50f);
	}

	/**
	 * Gets the computed value of a stat, including all applicable modifiers.
	 *
	 * @param statType The stat to get
	 * @return The computed stat value
	 */
	public float getStat(StatType statType) {
		// Return cached value if available and not dirty
		if (!cacheDirty && cachedValues.containsKey(statType)) {
			return cachedValues.get(statType);
		}

		float baseValue = getBaseStat(statType);
		float finalValue = baseValue;

		// Apply modifiers
		List<StatModifier> modifiers = statModifiers.getOrDefault(statType, Collections.emptyList());

		// First apply flat modifiers
		for (StatModifier mod : modifiers) {
			if (mod.getType() == StatModifier.ModifierType.FLAT) {
				finalValue += mod.getValue();
			}
		}

		// Then apply percentage modifiers (to base + flat modifiers)
		float percentMod = 0;
		for (StatModifier mod : modifiers) {
			if (mod.getType() == StatModifier.ModifierType.PERCENT) {
				percentMod += mod.getValue();
			}
		}

		if (percentMod != 0) {
			finalValue *= (1 + percentMod);
		}

		// Special handling for some stats
		if (statType == StatType.CURRENT_HEALTH) {
			finalValue = Math.min(finalValue, getStat(StatType.MAX_HEALTH));
			finalValue = Math.max(0, finalValue);
		}

		// Cache the result
		cachedValues.put(statType, finalValue);

		return finalValue;
	}

	/**
	 * Gets the base value of a stat without modifiers.
	 *
	 * @param statType The stat to get
	 * @return The base stat value
	 */
	public float getBaseStat(StatType statType) {
		if (baseStats.containsKey(statType)) {
			return baseStats.get(statType);
		}

		return defaultStats.getOrDefault(statType, 0f);
	}

	/**
	 * Sets the base value for a stat.
	 *
	 * @param statType The stat to set
	 * @param value The new base value
	 */
	public void setBaseStat(StatType statType, float value) {
		float oldValue = getStat(statType);
		baseStats.put(statType, value);
		cacheDirty = true;

		// Special handling for max health
		if (statType == StatType.MAX_HEALTH) {
			// Ensure current health doesn't exceed new max
			if (getStat(StatType.CURRENT_HEALTH) > value) {
				setBaseStat(StatType.CURRENT_HEALTH, value);
			}
		}

		// Notify listeners
		float newValue = getStat(statType);
		if (newValue != oldValue && statChangeListeners.containsKey(statType)) {
			statChangeListeners.get(statType).accept(newValue);
		}
	}

	/**
	 * Adds a modifier to a stat.
	 *
	 * @param statType The stat to modify
	 * @param modifier The modifier to add
	 */
	public void addModifier(StatType statType, StatModifier modifier) {
		float oldValue = getStat(statType);

		List<StatModifier> modifiers = statModifiers.computeIfAbsent(
			statType, k -> new ArrayList<>());
		modifiers.add(modifier);
		cacheDirty = true;

		// Notify listeners
		float newValue = getStat(statType);
		if (newValue != oldValue && statChangeListeners.containsKey(statType)) {
			statChangeListeners.get(statType).accept(newValue);
		}
	}

	/**
	 * Removes modifiers from a stat by source.
	 *
	 * @param statType The stat type
	 * @param source The source to remove
	 * @return Number of modifiers removed
	 */
	public int removeModifiers(StatType statType, String source) {
		List<StatModifier> modifiers = statModifiers.get(statType);
		if (modifiers == null) {
			return 0;
		}

		float oldValue = getStat(statType);
		int count = 0;

		Iterator<StatModifier> it = modifiers.iterator();
		while (it.hasNext()) {
			StatModifier mod = it.next();
			if (mod.getSource().equals(source)) {
				it.remove();
				count++;
			}
		}

		if (count > 0) {
			cacheDirty = true;

			// Notify listeners
			float newValue = getStat(statType);
			if (newValue != oldValue && statChangeListeners.containsKey(statType)) {
				statChangeListeners.get(statType).accept(newValue);
			}
		}

		return count;
	}

	/**
	 * Updates all temporary modifiers, removing expired ones.
	 * Should be called each frame.
	 */
	public void updateModifiers() {
		boolean removedAny = false;
		float deltaTime = (float) Time.getDeltaTime();

		for (Map.Entry<StatType, List<StatModifier>> entry : statModifiers.entrySet()) {
			StatType statType = entry.getKey();
			List<StatModifier> modifiers = entry.getValue();
			float oldValue = getStat(statType);

			// Update durations and track if any modifiers expired
			boolean removedFromStat = false;
			Iterator<StatModifier> it = modifiers.iterator();
			while (it.hasNext()) {
				StatModifier mod = it.next();
				if (!mod.isPermanent()) {
					mod.update(deltaTime);
					if (mod.isExpired()) {
						it.remove();
						removedFromStat = true;
						removedAny = true;
					}
				}
			}

			// Notify listeners if value changed due to expired modifiers
			if (removedFromStat) {
				float newValue = getStat(statType);
				if (newValue != oldValue && statChangeListeners.containsKey(statType)) {
					statChangeListeners.get(statType).accept(newValue);
				}
			}
		}

		if (removedAny) {
			cacheDirty = true;
		}
	}

	/**
	 * Sets the default value for a stat.
	 *
	 * @param statType The stat to set
	 * @param defaultValue The default value
	 */
	public void setDefaultStat(StatType statType, float defaultValue) {
		defaultStats.put(statType, defaultValue);
		cacheDirty = true;
	}

	/**
	 * Resets a stat to its default value.
	 *
	 * @param statType The stat to reset
	 */
	public void resetStat(StatType statType) {
		baseStats.remove(statType);
		statModifiers.remove(statType);
		cacheDirty = true;

		// Notify listeners
		if (statChangeListeners.containsKey(statType)) {
			statChangeListeners.get(statType).accept(getStat(statType));
		}
	}

	/**
	 * Modifies a stat by adding to its base value.
	 *
	 * @param statType The stat to modify
	 * @param amount The amount to add (negative to subtract)
	 */
	public void modifyStat(StatType statType, float amount) {
		float currentBase = getBaseStat(statType);
		setBaseStat(statType, currentBase + amount);
	}

	/**
	 * Checks if a stat is defined.
	 *
	 * @param statType The stat identifier
	 * @return True if the stat has a value or default
	 */
	public boolean hasStat(StatType statType) {
		return baseStats.containsKey(statType) || defaultStats.containsKey(statType);
	}

	/**
	 * Adds a listener for changes to a specific stat.
	 *
	 * @param statType The stat to listen for
	 * @param listener The callback to execute when the stat changes
	 */
	public void addStatChangeListener(StatType statType, Consumer<Float> listener) {
		statChangeListeners.put(statType, listener);
	}

	/**
	 * Removes a stat change listener.
	 *
	 * @param statType The stat that was being listened to
	 */
	public void removeStatChangeListener(StatType statType) {
		statChangeListeners.remove(statType);
	}

	/**
	 * Convenience method to get current health.
	 */
	public float getCurrentHealth() {
		return getStat(StatType.CURRENT_HEALTH);
	}

	/**
	 * Convenience method to set current health.
	 */
	public void setCurrentHealth(float health) {
		setBaseStat(StatType.CURRENT_HEALTH, health);
	}

	/**
	 * Convenience method to get max health.
	 */
	public float getMaxHealth() {
		return getStat(StatType.MAX_HEALTH);
	}

	/**
	 * Convenience method to set max health.
	 */
	public void setMaxHealth(float maxHealth) {
		setBaseStat(StatType.MAX_HEALTH, Math.max(1f, maxHealth));
	}

	/**
	 * Convenience method to get move speed.
	 */
	public float getMoveSpeed() {
		return getStat(StatType.MOVE_SPEED);
	}

	/**
	 * Convenience method to set move speed.
	 */
	public void setMoveSpeed(float speed) {
		setBaseStat(StatType.MOVE_SPEED, speed);
	}

	/**
	 * Convenience method to get damage.
	 */
	public float getDamage() {
		return getStat(StatType.DAMAGE);
	}

	/**
	 * Convenience method to set damage.
	 */
	public void setDamage(float damage) {
		setBaseStat(StatType.DAMAGE, damage);
	}

	/**
	 * Gets all stats as a map.
	 *
	 * @return A map of stat types to their computed values
	 */
	public Map<StatType, Float> getAllStats() {
		Map<StatType, Float> result = new EnumMap<>(StatType.class);

		// Include all defined stats (base or default)
		Set<StatType> allStatTypes = new HashSet<>();
		allStatTypes.addAll(baseStats.keySet());
		allStatTypes.addAll(defaultStats.keySet());

		// Get computed values for each stat
		for (StatType type : allStatTypes) {
			result.put(type, getStat(type));
		}

		return result;
	}
}