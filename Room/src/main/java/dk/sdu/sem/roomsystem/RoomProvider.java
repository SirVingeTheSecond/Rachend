package dk.sdu.sem.roomsystem;

import dk.sdu.sem.commonlevel.IRoomProvider;
import dk.sdu.sem.commonlevel.RoomParser;
import dk.sdu.sem.commonlevel.room.RoomInfo;

import java.util.List;

public class RoomProvider implements IRoomProvider {
	@Override
	public List<RoomInfo> getRooms() {
		return RoomParser.findAllRooms("Levels/");
	}
}
