package dk.sdu.sem.commonlevel;

import dk.sdu.sem.commonsystem.Scene;

public interface IRoomSPI {

	/**
	 * Creates a new scene from a random room with the openings specified
	 */
	Scene createRoom(boolean north, boolean east, boolean south, boolean west);


	/**
	 * Creates a specific room from name with the openings specified
	 */
	Scene createRoom(String roomName, boolean north, boolean east, boolean south, boolean west);
}
