package dk.sdu.sem.levelsystem.factories;

import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonsystem.Entity;

/**
 * Factory interface for creating barrier entities around the respective Room perimeter.
 */
public interface IBarrierFactory {
	/**
	 * Creates a fully configured barrier entity for a room.
	 *
	 * @param room The room to create a barrier for
	 * @param solidityChecker Function to check if a tile is solid
	 * @return Configured barrier entity
	 */
	Entity createBarrier(Room room, SolidityChecker solidityChecker);

	/**
	 * Functional interface for checking if a tile is solid.
	 */
	@FunctionalInterface
	interface SolidityChecker {
		boolean isSolid(Room room, int x, int y);
	}
}