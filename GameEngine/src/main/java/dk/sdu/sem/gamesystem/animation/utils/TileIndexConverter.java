package dk.sdu.sem.gamesystem.animation.utils;

import dk.sdu.sem.commonlevel.room.RoomData;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

/**
 * Utility class for converting between different tile ID representations.
 * Handles the conversion between global (Tiled) and local (0-based) tile IDs.
 */
public final class TileIndexConverter {
	private static final Logging LOGGER = Logging.createLogger("TileIndexConverter", LoggingLevel.DEBUG);

	/**
	 * Converts a global (Tiled) tile ID to a local (0-based) tile ID.
	 *
	 * @param globalId The global tile ID from Tiled
	 * @param firstGid The first global ID for the tileset
	 * @return The local tile ID (0-based)
	 */
	public static int globalToLocal(int globalId, int firstGid) {
		return globalId - firstGid;
	}

	/**
	 * Converts a local (0-based) tile ID to a global (Tiled) tile ID.
	 *
	 * @param localId The local tile ID (0-based)
	 * @param firstGid The first global ID for the tileset
	 * @return The global tile ID for Tiled
	 */
	public static int localToGlobal(int localId, int firstGid) {
		return localId + firstGid;
	}

	/**
	 * Gets the firstGid for a tileset at the given index in RoomData.
	 *
	 * @param roomData The room data containing tilesets
	 * @param tilesetIndex The index of the tileset
	 * @return The firstGid value, or 1 as default if not found
	 */
	public static int getFirstGid(RoomData roomData, int tilesetIndex) {
		if (roomData == null || tilesetIndex < 0 || tilesetIndex >= roomData.tilesets.size()) {
			return 1; // Default
		}

		try {
			return roomData.tilesets.get(tilesetIndex).firstgid;
		} catch (Exception e) {
			LOGGER.error("Error getting firstGid: " + e.getMessage());
			return 1; // Default
		}
	}

	/**
	 * Determines if a global tile ID belongs to a specific tileset.
	 *
	 * @param globalId The global tile ID
	 * @param cutPoints Array of first IDs for each tileset
	 * @param tilesetIndex The index of the tileset to check
	 * @return true if the tile belongs to the specified tileset
	 */
	public static boolean belongsToTileset(int globalId, int[] cutPoints, int tilesetIndex) {
		if (globalId <= 0 || cutPoints == null || tilesetIndex < 0 || tilesetIndex >= cutPoints.length) {
			return false;
		}

		boolean isAboveLowerBound = globalId > cutPoints[tilesetIndex];
		boolean isBelowUpperBound = (tilesetIndex == cutPoints.length - 1) ||
			(globalId < cutPoints[tilesetIndex + 1]);

		return isAboveLowerBound && isBelowUpperBound;
	}
}