package dk.sdu.sem.commonlevel;

import dk.sdu.sem.commonlevel.room.RoomData;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commontilemap.TilemapComponent;

/**
 * Interface for parsing tile animations from room data.
 */
public interface ITileAnimationParser {

	/**
	 * Extracts animation data from a tileset and applies it to an entity.
	 *
	 * @param entity The entity to add animation data to
	 * @param tilemapComponent The tilemap component with tile data
	 * @param roomData The room data containing tileset information
	 * @param tilesetIndex The index of the tileset to extract animations from
	 */
	void parseAndApplyAnimations(Entity entity, TilemapComponent tilemapComponent, RoomData roomData, int tilesetIndex);
}