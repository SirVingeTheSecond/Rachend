import dk.sdu.sem.commonlevel.room.IRoomClearedListener;
import dk.sdu.sem.commonsystem.IEntityLifecycleListener;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;

module Difficulty {
	uses dk.sdu.sem.commonitem.IItemFactory;
	uses dk.sdu.sem.commonlevel.ILevelSPI;
	requires CommonStats;
	requires GameEngine;
	requires CommonLevel;
	requires CommonEnemy;
    requires CommonPlayer;
	requires CommonInventory;
	requires CommonItem;
	requires Common;
	requires javafx.graphics;

	provides IRoomClearedListener with dk.sdu.sem.difficulty.DifficultySystem;
	provides IEntityLifecycleListener with dk.sdu.sem.difficulty.DifficultySystem;
	provides IGUIUpdate with dk.sdu.sem.difficulty.DifficultyUI;
}