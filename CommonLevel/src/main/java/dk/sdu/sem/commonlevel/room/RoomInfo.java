package dk.sdu.sem.commonlevel.room;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class RoomInfo {
	private int openings;
	private RoomData roomData;
	private String roomName;
	private RoomType roomType;

	public RoomInfo(String roomName, RoomData levelData, RoomType roomType, boolean north, boolean east, boolean south, boolean west) {
		this.roomData = levelData;
		this.openings = (north ? 1 : 0) | (east ? 2 : 0) | (south ? 4 : 0) | (west ? 8 : 0);
		this.roomName = roomName;
		this.roomType = roomType;
	}

	/**
	 * @return true if north is open, false if not
	 */
	public Boolean north() {
		return (openings & 1) != 0;
	}

	/**
	 * @return true if east is open, false if not
	 */
	public Boolean east() {
		return (openings & 2) != 0;
	}

	/**
	 * @return true if south is open, false if not
	 */
	public Boolean south() {
		return (openings & 4) != 0;
	}

	/**
	 * @return true if west is open, false if not
	 */
	public Boolean west() {
		return (openings & 8) != 0;
	}


	public RoomData getRoomData() {
		return roomData;
	}


	/**
	 * Returns the openings of the room in binary format:
	 * N-E-S-W, all four openings would be 1111b = 15,
	 * only north and east would be 1100b = 12
	 */
	public int getOpenings() {
		return openings;
	}

    public String getRoomName() {
        return roomName;
    }

	public RoomType getRoomType() {
		return roomType;
	}
}
