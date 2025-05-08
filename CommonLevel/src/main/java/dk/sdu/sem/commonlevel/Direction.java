package dk.sdu.sem.commonlevel;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Enum representing cardinal directions
 */
public enum Direction {
	NORTH(0, new Vector2D(0, -1)),
	EAST(1, new Vector2D(1, 0)),
	SOUTH(2, new Vector2D(0, 1)),
	WEST(3, new Vector2D(-1, 0)),
	NONE(-1, Vector2D.ZERO);

	private final int value;
	private final Vector2D unitVector;

	Direction(int value, Vector2D unitVector) {
		this.value = value;
		this.unitVector = unitVector;
	}

	public int getValue() {
		return value;
	}

	public Vector2D getUnitVector() {
		return unitVector;
	}

	public static String getDirectionName(int index) {
		return switch(index) {
			case 0 -> "north";
			case 1 -> "east";
			case 2 -> "south";
			case 3 -> "west";
			default -> "none";
		};
	}

	public Direction getOpposite() {
		if (this == NONE) return NONE;
		return values()[(ordinal() + 2) % 4];
	}

	/**
	 * Gets the offset for room positioning based on direction
	 */
	public Vector2D getRoomOffset(float roomWidth, float roomHeight) {
		return new Vector2D(
			unitVector.x() * roomWidth,
			unitVector.y() * roomHeight
		);
	}

	/**
	 * Gets the inverted offset for room positioning
	 */
	public Vector2D getInverseRoomOffset(float roomWidth, float roomHeight) {
		return new Vector2D(
			-unitVector.x() * roomWidth,
			-unitVector.y() * roomHeight
		);
	}

	/**
	 * Gets the entrance offset for player positioning
	 */
	public Vector2D getEntranceOffset(float offset) {
		return unitVector.scale(-offset);
	}
}