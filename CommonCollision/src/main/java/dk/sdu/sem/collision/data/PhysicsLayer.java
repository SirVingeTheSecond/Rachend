package dk.sdu.sem.collision.data;

/**
 * Defines physics layers for collision filtering.
 * Each layer represents a category of game objects for controlling which objects can collide with each other.
 */
public enum PhysicsLayer {
	DEFAULT(0),
	PLAYER(1),
	ENEMY(2),
	PROJECTILE(3),
	OBSTACLE(4),
	TRIGGER(5),
	ITEM(6),
	GROUND(7),
	DECORATION(8),
	NPC(9);

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