package dk.sdu.sem.roomsystem;

import dk.sdu.sem.commonlevel.IRoomProvider;
import dk.sdu.sem.commonlevel.IRoomSPI;
import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonsystem.Scene;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class RoomManager implements IRoomSPI {
	private final RoomGenerator parser = new RoomGenerator();
	private final List<Room> rooms = new ArrayList<>();

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
		for (Room room : rooms) {
			if ((room.getOpenings() & key) == key) {
				result.add(new Room(room.getRoomData(), north, east, south, west));
			}
		}
		return result;
	}

	public Room getRandomRoom(boolean north, boolean east, boolean south, boolean west) {
		int key = (north ? 1 : 0) | (east ? 2 : 0) | (south ? 4 : 0) | (west ? 8 : 0);

		List<Room> filtered = rooms.stream()
			.filter(room -> (room.getOpenings() & key) == key)
			.toList();

		if (filtered.isEmpty()) {
			System.out.println("No room found");
			return null;
		}

		Room room = filtered.get((int)(Math.random() * filtered.size()));

		return new Room(room.getRoomData(), north, east, south, west);
	}

	private void addRoom(Room room) {
		rooms.add(room);
	}

	@Override
	public Scene createRoom(boolean north, boolean east, boolean south, boolean west) {
		Room room = getRandomRoom(north, east, south, west);
		if (room == null)
			return new Scene("empty");

		return parser.createRoomScene(room);
	}
}
