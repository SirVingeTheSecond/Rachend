module Level {
	requires GameEngine;
	requires CommonLevel;
	requires CommonPlayer;
	requires Common;

	uses dk.sdu.sem.commonlevel.IRoomSPI;

	provides dk.sdu.sem.commonlevel.ILevelSPI with dk.sdu.sem.levelsystem.LevelManager;

	provides dk.sdu.sem.gamesystem.services.IUpdate with
		dk.sdu.sem.levelsystem.LevelManager,
		dk.sdu.sem.levelsystem.RoomTransitionSystem;

	exports dk.sdu.sem.levelsystem;
}