package dk.sdu.sem.commonlevel.room;

public enum ZoneType {
	ENEMY(0),
	NORTH_ENTRANCE(1),
	EAST_ENTRANCE(2),
	SOUTH_ENTRANCE(3),
	WEST_ENTRANCE(4),
	PROPS(5);

	private int numVal;

	ZoneType(int numVal) {
		this.numVal = numVal;
	}

	public int getNumVal() {
		return numVal;
	}

	public static ZoneType getZoneByNumVal(int numVal) {
		if (numVal < 0 || numVal > ZoneType.values().length - 1)
			return null;

		return ZoneType.values()[numVal];
	}
}
