package dk.sdu.sem.commonstats;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.gamesystem.Time;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

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
	private final Map<StatType, List<BiConsumer<Float, Float>>> statChangeListeners = new ConcurrentHashMap<>();

	// Cached computed values (for performance)
	private final Map<StatType, Float> cachedValues = new EnumMap<>(StatType.class);
	private boolean cacheDirty = true;

	private float currentHealth;

	/**
	 * Creates a new stats component with default values.
	 */
	public StatsComponent() {
		// Set up default stats
		currentHealth = 100f;
		setDefaultStat(StatType.MAX_HEALTH, 100f);
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
		if (statType == StatType.CURRENT_HEALTH) {
			return getCurrentHealth();
		}

		// Return cached value if available and not dirty
		if (!cacheDirty && cachedValues.containsKey(statType)) {
			return cachedValues.get(statType);
		}

		float finalValue = getBaseStat(statType);

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

		// Multiplicative
		for (StatModifier mod : modifiers) {
			if (mod.getType() == StatModifier.ModifierType.MULTIPLICATIVE) {
				finalValue *= mod.getValue();
			}
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
		if (statType == StatType.CURRENT_HEALTH) {
			return getCurrentHealth();
		}

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
		if (statType == StatType.CURRENT_HEALTH) {
			setCurrentHealth(value);
			return;
		}

		float oldValue = getStat(statType);
		baseStats.put(statType, value);
		cacheDirty = true;

		// Special handling for max health
		if (statType == StatType.MAX_HEALTH) {
			// Ensure current health doesn't exceed new max
			if (currentHealth > value) {
				currentHealth = value;
			}
		}

		// Notify listeners
		float newValue = getStat(statType);
		notifyStatChangeListener(statType, oldValue, newValue);
	}

	/**
	 * Adds a modifier to a stat. Can not modify the CURRENT_HEALTH stat!
	 *
	 * @param statType The stat to modify
	 * @param modifier The modifier to add
	 */
	public void addModifier(StatType statType, StatModifier modifier) {
		assert statType != StatType.CURRENT_HEALTH;

		float oldValue = getStat(statType);

		List<StatModifier> modifiers = statModifiers.computeIfAbsent(
			statType, k -> new ArrayList<>());
		modifiers.add(modifier);
		cacheDirty = true;

		// Notify listeners
		float newValue = getStat(statType);
		notifyStatChangeListener(statType, oldValue, newValue);
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
			notifyStatChangeListener(statType, oldValue, newValue);
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
				notifyStatChangeListener(statType, oldValue, newValue);
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
		float oldValue = getStat(statType);
		baseStats.remove(statType);
		statModifiers.remove(statType);
		cacheDirty = true;

		// Notify listeners
		notifyStatChangeListener(statType, oldValue, getStat(statType));
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
	public void addStatChangeListener(StatType statType, BiConsumer<Float, Float> listener) {
		statChangeListeners.computeIfAbsent(statType, e -> new ArrayList<>()).add(listener);
	}

	/**
	 * Removes a stat change listener.
	 *
	 * @param statType The stat that was being listened to
	 * @param listener the BiConsumer listener to remove
	 */
	public void removeStatChangeListener(StatType statType, BiConsumer<Float, Float> listener) {
		statChangeListeners.get(statType).remove(listener);
	}

	/**
	 * Notifies the listeners of a given stat type
	 * @param statType which stat was changed
	 * @param oldValue value before the stat change
	 * @param newValue value after the stat change
	 */
	private void notifyStatChangeListener(StatType statType, float oldValue, float newValue) {
		if (oldValue == newValue)
			return;

		var listeners = statChangeListeners.get(statType);
		if (listeners == null)
			return;

		for (var l : new ArrayList<>(listeners)) {
			l.accept(oldValue, newValue);
		}
	}

	/**
	 * Convenience method to get current health.
	 */
	public float getCurrentHealth() {
		return currentHealth;
	}

	/**
	 * Convenience method to set current health.
	 */
	public void setCurrentHealth(float health) {
		health = Math.max(health, 0);
		health = Math.min(health, getStat(StatType.MAX_HEALTH));

		float oldValue = currentHealth;
		currentHealth = health;

		notifyStatChangeListener(StatType.CURRENT_HEALTH, oldValue, currentHealth);
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