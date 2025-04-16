import dk.sdu.sem.commonlevel.IRoomProvider;
import dk.sdu.sem.commonlevel.IRoomSPI;
import dk.sdu.sem.gamesystem.services.IStart;

module Room {
	uses dk.sdu.sem.commonlevel.IRoomProvider;
	requires com.fasterxml.jackson.databind;
	requires GameEngine;
	requires CommonLevel;
	requires CommonCollision;
	requires Common;
	requires java.sql;

	exports dk.sdu.sem.roomsystem;

	provides IRoomSPI with dk.sdu.sem.roomsystem.RoomManager;
	provides IRoomProvider with dk.sdu.sem.roomsystem.RoomProvider;
}
