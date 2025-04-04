package dk.sdu.sem.commonlevel;

import dk.sdu.sem.commonsystem.Scene;

public interface IRoomSPI {
	Scene createRoom(boolean north, boolean east, boolean south, boolean west);
}
