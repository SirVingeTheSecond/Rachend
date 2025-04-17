package dk.sdu.sem.roomsystem;

import dk.sdu.sem.commonlevel.IRoomProvider;
import dk.sdu.sem.commonlevel.IRoomSPI;
import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonlevel.room.RoomType;
import dk.sdu.sem.commonsystem.Scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ServiceLoader;

public class RoomManager implements IRoomSPI {
	private final RoomGenerator parser = new RoomGenerator();
	private final HashMap<String, Room> rooms = new HashMap<>();
	private final HashMap<RoomType, List<Room>> roomTypeListHashMap = new HashMap<>();

	public RoomManager() {
		List<Room> rooms = new ArrayList<>();
		ServiceLoader.load(IRoomProvider.class).forEach(provider -> {
			rooms.addAll(provider.getRooms());
		});
		for (Room room : rooms) {
			addRoom(room);
		}
	}

	public List<Room> getRooms(boolean north, boolean east, boolean south, boolean west) {
		int key = (north ? 1 : 0) | (east ? 2 : 0) | (south ? 4 : 0) | (west ? 8 : 0);
		List<Room> result = new ArrayList<>();
		for (Room room : rooms.values()) {
			if ((room.getOpenings() & key) == key) {
				result.add(new Room(room.getRoomName(), room.getRoomData(), room.getRoomType(), north, east, south, west));
			}
		}
		return result;
	}

	public Room getRandomRoom(boolean north, boolean east, boolean south, boolean west) {
		//Binary representation of the room openings
		int key = (north ? 1 : 0) | (east ? 2 : 0) | (south ? 4 : 0) | (west ? 8 : 0);

		//Filter by applying bit-masking with the key
		List<Room> filtered = rooms.values().stream()
			.filter(room -> (room.getOpenings() & key) == key)
			.toList();

		if (filtered.isEmpty()) {
			throw new RuntimeException("No Rooms supports openings");
		}

		Room room = filtered.get((int)(Math.random() * filtered.size()));

		return new Room(room.getRoomName(), room.getRoomData(), room.getRoomType(), north, east, south, west);
	}

	private void addRoom(Room room) {
		rooms.put(room.getRoomName(), room);
		roomTypeListHashMap.computeIfAbsent(room.getRoomType(), k -> new ArrayList<>()).add(room);
	}

	@Override
	public Scene createRoom(boolean north, boolean east, boolean south, boolean west) {
		Room room = getRandomRoom(north, east, south, west);
		if (room == null)
			return new Scene("empty");

		return parser.createRoomScene(room);
	}

	@Override
	public Scene createRoom(String roomName, boolean north, boolean east, boolean south, boolean west) {
		Room room = rooms.get(roomName);
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
		Room result = new Room(room.getRoomName(), room.getRoomData(), room.getRoomType(), north, east, south, west);
		return parser.createRoomScene(result);
	}

	@Override
	public Scene createRoom(RoomType roomType, boolean north, boolean east, boolean south, boolean west) {
		List<Room> rooms = roomTypeListHashMap.get(roomType);
		if (rooms == null || rooms.isEmpty()) {
			System.out.println("No rooms found for room type: " + roomType + " using random room instead");
			return createRoom(north, east, south, west);
		}

		//Binary representation of the room openings
		int key = (north ? 1 : 0) | (east ? 2 : 0) | (south ? 4 : 0) | (west ? 8 : 0);

		//Filter by applying bit-masking with the key
		List<Room> filtered = rooms.stream()
			.filter(room -> (room.getOpenings() & key) == key)
			.toList();

		if (filtered.isEmpty()) {
			throw new RuntimeException("No Rooms of type: " + roomType + " supports openings");
		}

		Room temp = filtered.get((int)(Math.random() * filtered.size()));

		Room room = new Room(temp.getRoomName(), temp.getRoomData(), temp.getRoomType(), north, east, south, west);

		return parser.createRoomScene(room);
	}
}
