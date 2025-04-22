module CommonLevel {
	uses dk.sdu.sem.commonlevel.IRoomSPI;
	requires Common;
	requires com.fasterxml.jackson.databind;
	exports dk.sdu.sem.commonlevel;
	exports dk.sdu.sem.commonlevel.room;
}