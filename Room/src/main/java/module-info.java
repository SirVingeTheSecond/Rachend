import dk.sdu.sem.commonlevel.IRoomProvider;
import dk.sdu.sem.commonlevel.IRoomSPI;
import dk.sdu.sem.commonlevel.room.IRoomCreatedListener;
import dk.sdu.sem.commonsystem.INodeProvider;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
import dk.sdu.sem.gamesystem.services.IUpdate;

module Room {
	uses dk.sdu.sem.commonlevel.IRoomProvider;
	uses dk.sdu.sem.enemy.IEnemyFactory;
	uses dk.sdu.sem.collision.IColliderFactory;
	uses dk.sdu.sem.commonlevel.room.IRoomCreatedListener;
	uses dk.sdu.sem.commonlevel.ITileAnimationParser;
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
	provides IRoomCreatedListener with dk.sdu.sem.roomsystem.RoomSystem;
	provides IUpdate with dk.sdu.sem.roomsystem.RoomSystem;
	provides Node with dk.sdu.sem.roomsystem.EnemyNode;
	provides INodeProvider with dk.sdu.sem.roomsystem.EnemyNode;
	provides IAssetProvider with dk.sdu.sem.roomsystem.RoomAssetProvider;
}
