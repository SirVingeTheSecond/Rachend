module Level {
	uses dk.sdu.sem.commonlevel.IRoomSPI;
	requires GameEngine;
	requires CommonLevel;
	requires CommonPlayer;
	requires Common;

	provides dk.sdu.sem.commonlevel.ILevelSPI with dk.sdu.sem.levelsystem.LevelManager;
	provides dk.sdu.sem.gamesystem.services.IUpdate with dk.sdu.sem.levelsystem.LevelManager;
}