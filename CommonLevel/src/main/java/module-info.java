module CommonLevel {
	uses dk.sdu.sem.commonlevel.IRoomSPI;

    requires com.fasterxml.jackson.databind;
    requires CommonTilemap;
	requires Common;
	requires javafx.graphics;

	exports dk.sdu.sem.commonlevel;
	exports dk.sdu.sem.commonlevel.room;
	exports dk.sdu.sem.commonlevel.events;
	exports dk.sdu.sem.commonlevel.components;
}