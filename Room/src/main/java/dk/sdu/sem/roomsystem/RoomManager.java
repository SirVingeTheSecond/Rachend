package dk.sdu.sem.roomsystem;

import dk.sdu.sem.commonlevel.IRoomProvider;
import dk.sdu.sem.commonlevel.IRoomSPI;
import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonlevel.room.RoomInfo;
import dk.sdu.sem.commonlevel.room.RoomType;
import dk.sdu.sem.commonsystem.Scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ServiceLoader;

public class RoomManager implements IRoomSPI {
	private static RoomManager instance;

	private static final RoomGenerator parser = new RoomGenerator();
	private static final HashMap<String, RoomInfo> rooms = new HashMap<>();
	private static final HashMap<RoomType, List<RoomInfo>> roomTypeListHashMap = new HashMap<>();

	public RoomManager() {
		if (instance != null)
			return;

		List<RoomInfo> rooms = new ArrayList<>();
		ServiceLoader.load(IRoomProvider.class).forEach(provider -> {
			rooms.addAll(provider.getRooms());
		});
		for (RoomInfo room : rooms) {
			addRoom(room);
		}
		instance = this;
	}

	public List<RoomInfo> getRooms(boolean north, boolean east, boolean south, boolean west) {
		int key = (north ? 1 : 0) | (east ? 2 : 0) | (south ? 4 : 0) | (west ? 8 : 0);
		List<RoomInfo> result = new ArrayList<>();
		for (RoomInfo room : rooms.values()) {
			if ((room.getOpenings() & key) == key) {
				result.add(new RoomInfo(room.getRoomName(), room.getRoomData(), room.getRoomType(), north, east, south, west));
			}
		}
		return result;
	}

	public RoomInfo getRandomRoom(boolean north, boolean east, boolean south, boolean west) {
		//Binary representation of the room openings
		int key = (north ? 1 : 0) | (east ? 2 : 0) | (south ? 4 : 0) | (west ? 8 : 0);

		//Filter by applying bit-masking with the key
		List<RoomInfo> filtered = rooms.values().stream()
			.filter(room -> (room.getOpenings() & key) == key)
			.toList();

		if (filtered.isEmpty()) {
			throw new RuntimeException("No Rooms supports openings");
		}

		RoomInfo room = filtered.get((int)(Math.random() * filtered.size()));

		return new RoomInfo(room.getRoomName(), room.getRoomData(), room.getRoomType(), north, east, south, west);
	}

	private void addRoom(RoomInfo room) {
		if (room == null) {
			System.err.println("Attempted to add null room, skipping");
			return;
		}
		rooms.put(room.getRoomName(), room);
		roomTypeListHashMap.computeIfAbsent(room.getRoomType(), k -> new ArrayList<>()).add(room);
	}

	@Override
	public Room createRoom(boolean north, boolean east, boolean south, boolean west) {
		RoomInfo room = getRandomRoom(north, east, south, west);
		if (room == null)
			return new Room(new Scene("empty"));

		return parser.createRoomScene(room);
	}

	@Override
	public Room createRoom(String roomName, boolean north, boolean east, boolean south, boolean west) {
		RoomInfo room = rooms.get(roomName);
		if (room == null) {
			throw new RuntimeException("Room not found: " + roomName);
		}

		//Binary representation of the room openings
		int key = (north ? 1 : 0) | (east ? 2 : 0) | (south ? 4 : 0) | (west ? 8 : 0);

		//Check if the room specified support these openings
		if ((room.getOpenings() & key) != key) {
			throw new RuntimeException("Room " + roomName + " does not support openings");
		}

		//Copy the room found, and change openings
		RoomInfo result = new RoomInfo(room.getRoomName(), room.getRoomData(), room.getRoomType(), north, east, south, west);
		return parser.createRoomScene(result);
	}

	@Override
	public Room createRoom(RoomType roomType, boolean north, boolean east, boolean south, boolean west) {
		List<RoomInfo> rooms = roomTypeListHashMap.get(roomType);
		if (rooms == null || rooms.isEmpty()) {
			System.out.println("No rooms found for room type: " + roomType + " using random room instead");
			return createRoom(north, east, south, west);
		}

		//Binary representation of the room openings
		int key = (north ? 1 : 0) | (east ? 2 : 0) | (south ? 4 : 0) | (west ? 8 : 0);

		//Filter by applying bit-masking with the key
		List<RoomInfo> filtered = rooms.stream()
			.filter(room -> (room.getOpenings() & key) == key)
			.toList();

		if (filtered.isEmpty()) {
			throw new RuntimeException("No Rooms of type: " + roomType + " supports openings");
		}

		RoomInfo temp = filtered.get((int)(Math.random() * filtered.size()));

		RoomInfo room = new RoomInfo(temp.getRoomName(), temp.getRoomData(), temp.getRoomType(), north, east, south, west);

		return parser.createRoomScene(room);
	}
}
