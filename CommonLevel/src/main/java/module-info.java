module CommonLevel {
	uses dk.sdu.sem.commonlevel.IRoomSPI;
    requires com.fasterxml.jackson.databind;
    requires CommonTilemap;
	requires Common;
	exports dk.sdu.sem.commonlevel;
	exports dk.sdu.sem.commonlevel.room;
}