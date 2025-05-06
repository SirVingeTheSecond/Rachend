package dk.sdu.sem.commonstats;

/**
 * Enum defining all available stat types with categories.
 */
public enum StatType {

	// Health stats
	MAX_HEALTH(StatCategory.HEALTH),
	CURRENT_HEALTH(StatCategory.HEALTH),
	ARMOR(StatCategory.HEALTH),

	// Combat stats
	DAMAGE(StatCategory.COMBAT),
	ATTACK_SPEED(StatCategory.COMBAT),
	ATTACK_RANGE(StatCategory.COMBAT),

	// Movement stats
	MOVE_SPEED(StatCategory.MOVEMENT),

	// Bullet stats
	BULLET_SPEED(StatCategory.BULLET),
	LIFETIME(StatCategory.BULLET),
	BULLET_SCALE(StatCategory.BULLET),
	BULLET_KNOCKBACK(StatCategory.BULLET),

	// Item stats
	HEAL_AMOUNT(StatCategory.ITEM),
	SPEED_BOOST(StatCategory.ITEM);

	// Semantically, this might be a bit cursed
	private final StatCategory category;

	StatType(StatCategory category) {
		this.category = category;
	}

	public StatCategory getCategory() {
		return category;
	}

	/**
	 * Categories for stats.
	 */
	public enum StatCategory {
		HEALTH,
		COMBAT,
		MOVEMENT,
		BULLET,
		ITEM
	}
}