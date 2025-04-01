import dk.sdu.sem.commonlevel.ILevelSPI;

module Level {
	requires com.fasterxml.jackson.databind;
	requires GameEngine;
	requires CommonLevel;
	requires CommonCollision;
	requires Common;
    requires java.sql;

    exports dk.sdu.sem.levelsystem.parsing;
	exports dk.sdu.sem.levelsystem.parsing.dto to com.fasterxml.jackson.databind;

	provides ILevelSPI with dk.sdu.sem.levelsystem.parsing.LevelParser;
}