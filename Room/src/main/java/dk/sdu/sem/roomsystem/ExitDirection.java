package dk.sdu.sem.roomsystem;

public enum ExitDirection {
	NORTH, SOUTH, EAST, WEST;

	public static ExitDirection opposite(ExitDirection direction) {
		return switch (direction) {
			case NORTH -> SOUTH;
			case SOUTH -> NORTH;
			case EAST -> WEST;
			case WEST -> EAST;
			default:
            	throw new IllegalStateException("No can't do this: " + direction);
		};
	}


	public static ExitDirection random() {
		ExitDirection[] directions = values();
		return directions[(int) (Math.random() * directions.length)];
	}

	public ExitDirection opposite() {
		return ExitDirection.opposite(this);
	}
}
