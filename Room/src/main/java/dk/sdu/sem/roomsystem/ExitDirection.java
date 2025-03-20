package dk.sdu.sem.roomsystem;

public enum ExitDirection {
	NORTH, SOUTH, EAST, WEST;

	public static ExitDirection opposite(ExitDirection direction) {
		return switch (direction) {
			case NORTH -> SOUTH;
			case SOUTH -> NORTH;
			case EAST -> WEST;
			case WEST -> EAST;
		};
	}


	public static ExitDirection random() {
		return switch ((int) (Math.random() * 4)) {
			case 0 -> NORTH;
			case 1 -> SOUTH;
			case 2 -> EAST;
			case 3 -> WEST;
			default -> throw new IllegalStateException("Unexpected value");
		};
	}

	public ExitDirection opposite() {
		return ExitDirection.opposite(this);
	}
}
