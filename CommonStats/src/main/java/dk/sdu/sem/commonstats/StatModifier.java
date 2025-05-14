package dk.sdu.sem.commonstats;

/**
 * Represents a modifier that can be applied to a stat.
 * Modifiers have a source, type and value.
 */
public class StatModifier {
	private final String source;
	private final ModifierType type;
	private final float value;
	private final float duration; // In seconds, -1 for permanent
	private float remainingTime;

	public enum ModifierType {
		FLAT, // simple value
		PERCENT, // percentage of base value
		MULTIPLICATIVE, // multiplicative after flat and percent
	}

	public StatModifier(String source, ModifierType type, float value, float duration) {
		this.source = source;
		this.type = type;
		this.value = value;
		this.duration = duration;
		this.remainingTime = duration;
	}

	public static StatModifier createFlat(String source, float value, float duration) {
		return new StatModifier(source, ModifierType.FLAT, value, duration);
	}

	public static StatModifier createPercent(String source, float value, float duration) {
		return new StatModifier(source, ModifierType.PERCENT, value, duration);
	}

	public static StatModifier createPermanentFlat(String source, float value) {
		return new StatModifier(source, ModifierType.FLAT, value, -1);
	}

	public static StatModifier createPermanentPercent(String source, float value) {
		return new StatModifier(source, ModifierType.PERCENT, value, -1);
	}

	public static StatModifier createMultiplicative(String source, float value, float duration) {
		return new StatModifier(source, ModifierType.MULTIPLICATIVE, value, duration);
	}

	public static StatModifier createPermanentMultiplicative(String source, float value) {
		return new StatModifier(source, ModifierType.MULTIPLICATIVE, value, -1);
	}

	public String getSource() {
		return source;
	}

	public ModifierType getType() {
		return type;
	}

	public float getValue() {
		return value;
	}

	public float getDuration() {
		return duration;
	}

	public float getRemainingTime() {
		return remainingTime;
	}

	public boolean isPermanent() {
		return duration < 0;
	}

	public boolean isExpired() {
		return !isPermanent() && remainingTime <= 0;
	}

	public void update(float deltaTime) {
		if (!isPermanent()) {
			remainingTime -= deltaTime;
		}
	}
}
