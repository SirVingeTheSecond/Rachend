package dk.sdu.sem.commonlevel;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonlevel.room.RoomData;
import dk.sdu.sem.commonlevel.room.RoomLayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RoomParser {
	public static List<Room> findAllRooms(String path) {
		File folder = new File(path);
		if (!folder.exists() || !folder.isDirectory())
			return List.of();

		List<Room> rooms = new ArrayList<>();
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				rooms.addAll(findAllRooms(file.getPath()));// Recursive call for subfolders
			} else if (file.getName().endsWith(".json")) {
				rooms.add(findRoom(file));
			}
		}
		return rooms;
	}

	public static Room findRoom(File levelData) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			RoomData roomData = mapper.readValue(levelData, RoomData.class);

			boolean[] openings = getRoomOpenings(roomData);

			return new Room(
				levelData.getName(),
				roomData,
				openings[0],
				openings[1],
				openings[2],
				openings[3]
			);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	private static boolean[] getRoomOpenings(RoomData roomData) {
		boolean[] possibleOpenings = new boolean[4];

		for (RoomLayer layer : roomData.layers) {
			switch (layer.name) {
				case "DOOR_NORTH":
					possibleOpenings[0] = true;
					break;
				case "DOOR_SOUTH":
					possibleOpenings[2] = true;
					break;
				case "DOOR_EAST":
					possibleOpenings[1] = true;
					break;
				case "DOOR_WEST":
					possibleOpenings[3] = true;
					break;
			}
		}

		return possibleOpenings;
	}
}
