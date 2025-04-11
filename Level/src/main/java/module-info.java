module Level {
	uses dk.sdu.sem.commonlevel.IRoomSPI;
	requires GameEngine;
	requires CommonLevel;
	requires Common;

	provides dk.sdu.sem.commonlevel.ILevelSPI with dk.sdu.sem.levelsystem.LevelManager;
}