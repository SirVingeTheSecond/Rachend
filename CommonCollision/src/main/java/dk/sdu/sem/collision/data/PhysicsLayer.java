package dk.sdu.sem.collision.data;

/**
 * Defines physics layers for collision filtering.
 * Each layer represents a category of game objects for controlling which objects can collide with each other.
 */
public enum PhysicsLayer {
	DEFAULT(0),
	PLAYER(1),
	ENEMY(2),
	ENEMY_PROJECTILE(3),
	PLAYER_PROJECTILE(4),
	OBSTACLE(5),
	TRIGGER(6),
	ITEM(7),
	GROUND(8),
	DECORATION(9),
	NPC(10),
	HOLE(11);

	private final int value;

	PhysicsLayer(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	/**
	 * Gets a physics layer by its numeric value.
	 *
	 * @param value The layer value
	 * @return The corresponding PhysicsLayer or DEFAULT if not found
	 */
	public static PhysicsLayer fromValue(int value) {
		for (PhysicsLayer layer : values()) {
			if (layer.value == value) {
				return layer;
			}
		}
		return DEFAULT;
	}
}