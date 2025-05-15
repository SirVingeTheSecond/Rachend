package dk.sdu.sem.levelsystem.factories;

import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.levelsystem.factories.IBarrierFactory.SolidityChecker;

/**
 * Service for populating barrier maps based on room properties.
 */
public class BarrierPopulator {

	/**
	 * Populates collision and render maps based on solid tile checks.
	 * Places barrier tiles where the room edges should be blocked.
	 *
	 * @param room The room to create maps for
	 * @param collisionMap The collision map to populate
	 * @param renderMap The render map to populate
	 * @param solidityChecker Function to check if a tile is solid
	 */
	public void populateMaps(Room room, int[][] collisionMap, int[][] renderMap,
							 SolidityChecker solidityChecker) {
		int worldWidth = (int)GameConstants.WORLD_SIZE.x();
		int worldHeight = (int)GameConstants.WORLD_SIZE.y();

		// Process top and bottom edges
		for (int x = 0; x < worldWidth; x++) {
			// Top edge
			if (!solidityChecker.isSolid(room, x, 0)) {
				collisionMap[x][0] = 1;
				renderMap[x][0] = 0;
			}

			// Bottom edge
			int y = worldHeight - 1;
			if (!solidityChecker.isSolid(room, x, y)) {
				collisionMap[x][y] = 1;
				renderMap[x][y] = 0;
			}
		}

		// Process left and right edges
		for (int y = 0; y < worldHeight; y++) {
			// Left edge
			if (!solidityChecker.isSolid(room, 0, y)) {
				collisionMap[0][y] = 1;
				renderMap[0][y] = 0;
			}

			// Right edge
			int x = worldWidth - 1;
			if (!solidityChecker.isSolid(room, x, y)) {
				collisionMap[x][y] = 1;
				renderMap[x][y] = 0;
			}
		}
	}
}