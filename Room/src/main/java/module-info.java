import dk.sdu.sem.commonlevel.IRoomProvider;
import dk.sdu.sem.commonlevel.IRoomSPI;
import dk.sdu.sem.gamesystem.services.IStart;
import dk.sdu.sem.gamesystem.services.IUpdate;

module Room {
	uses dk.sdu.sem.commonlevel.IRoomProvider;
	uses dk.sdu.sem.enemy.IEnemyFactory;
	requires com.fasterxml.jackson.databind;
	requires GameEngine;
	requires CommonLevel;
    requires java.sql;
	requires CommonEnemy;
    requires CommonPlayer;
	requires CommonCollision;
	requires CommonTilemap;
	requires Common;

	exports dk.sdu.sem.roomsystem;

	provides IRoomSPI with dk.sdu.sem.roomsystem.RoomManager;
	provides IRoomProvider with dk.sdu.sem.roomsystem.RoomProvider;
	provides IUpdate with dk.sdu.sem.roomsystem.LevelTest;
	provides IStart with dk.sdu.sem.roomsystem.LevelTest;
}
