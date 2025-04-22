package dk.sdu.sem.commonlevel;

import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonlevel.room.RoomType;

public interface IRoomSPI {

	/**
	 * Creates a new scene from a random room with the openings specified
	 */
	Room createRoom(boolean north, boolean east, boolean south, boolean west);


	/**
	 * Creates a specific room from name with the openings specified
	 */
	Room createRoom(String roomName, boolean north, boolean east, boolean south, boolean west);

	/**
	 * Creates a random room of type with the openings specified
	 */
	Room createRoom(RoomType roomType, boolean north, boolean east, boolean south, boolean west);
}
