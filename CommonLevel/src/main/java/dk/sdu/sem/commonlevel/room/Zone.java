package dk.sdu.sem.commonlevel.room;

public enum Zone {
	ENEMY_SPAWN_POINT(0),
	NORTH_ENTRANCE(1),
	EAST_ENTRANCE(2),
	SOUTH_ENTRANCE(3),
	WEST_ENTRANCE(4),
	PROP_SPAWN_POINT(5);

	private int numVal;

	Zone(int numVal) {
		this.numVal = numVal;
	}

	public int getNumVal() {
		return numVal;
	}

	public static Zone getZoneByNumVal(int numVal) {
		if (numVal < 0 || numVal > Zone.values().length - 1)
			return null;

		return Zone.values()[numVal];
	}
}
