import dk.sdu.sem.levelsystem.managers.LevelManager;
import dk.sdu.sem.levelsystem.managers.RoomManager;
import dk.sdu.sem.levelsystem.providers.RoomAssetProvider;
import dk.sdu.sem.levelsystem.providers.RoomProvider;
import dk.sdu.sem.levelsystem.systems.RoomSystem;
import dk.sdu.sem.levelsystem.systems.RoomTransitionSystem;

module Level {
	requires GameEngine;
	requires CommonLevel;
	requires CommonPlayer;
	requires Common;
	requires CommonEnemy;
	requires CommonCollision;
	requires CommonTilemap;
	requires java.sql;
	requires com.fasterxml.jackson.databind;
	requires javafx.graphics;

	provides dk.sdu.sem.commonlevel.ILevelSPI with LevelManager;
	provides dk.sdu.sem.commonlevel.IRoomSPI with RoomManager;
	provides dk.sdu.sem.commonlevel.IRoomProvider with RoomProvider;
	provides dk.sdu.sem.commonlevel.room.IRoomCreatedListener with RoomSystem;
	provides dk.sdu.sem.commonsystem.INodeProvider with dk.sdu.sem.levelsystem.EnemyNode;
	provides dk.sdu.sem.commonsystem.Node with dk.sdu.sem.levelsystem.EnemyNode;
	provides dk.sdu.sem.gamesystem.services.IUpdate with
		LevelManager,
		RoomTransitionSystem,
		RoomSystem;
	provides dk.sdu.sem.gamesystem.services.IStart with RoomSystem;
	provides dk.sdu.sem.gamesystem.assets.providers.IAssetProvider with RoomAssetProvider;
	provides dk.sdu.sem.levelsystem.factories.IBarrierFactory with dk.sdu.sem.levelsystem.factories.BarrierFactory;

	uses dk.sdu.sem.commonlevel.IRoomSPI;
	uses dk.sdu.sem.enemy.IEnemyFactory;
	uses dk.sdu.sem.collision.IColliderFactory;
	uses dk.sdu.sem.commonlevel.room.IRoomCreatedListener;
	uses dk.sdu.sem.commonlevel.ITileAnimationParser;
	uses dk.sdu.sem.commonlevel.room.IRoomClearedListener;
	uses dk.sdu.sem.commonlevel.IRoomProvider;
	uses dk.sdu.sem.levelsystem.factories.IBarrierFactory;

	exports dk.sdu.sem.levelsystem;
	exports dk.sdu.sem.levelsystem.systems;
	exports dk.sdu.sem.levelsystem.providers;
	exports dk.sdu.sem.levelsystem.managers;
	exports dk.sdu.sem.levelsystem.factories;
}