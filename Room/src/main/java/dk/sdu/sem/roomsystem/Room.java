package dk.sdu.sem.roomsystem;

import java.util.*;
import java.util.function.Consumer;

public class Room {
	private EnumMap<ExitDirection, Room> neighbours = new EnumMap<>(ExitDirection.class);

	/// Connects two rooms together
	/// @param from The room to connect from
	/// @param to The room to connect to
	/// @param direction The direction to connect the rooms in
	static void connect(Room from, ExitDirection direction, Room to) {
		from.neighbours.put(direction, to);
		to.neighbours.put(direction.opposite(), from);
	}

	/// Traverses the room and all its neighbours
	/// @param consumer The consumer to apply to each room
	public void traverse(Consumer<Room> consumer) {
		Queue<Room> queue = new LinkedList<>();
		Set<Room> visited = new HashSet<>();
		queue.add(this);
		visited.add(this);

		while (!queue.isEmpty()) {
			Room room = queue.poll();
			consumer.accept(room);

            for (Room neighbor : room.neighbours.values()) {
                if (neighbor != null && visited.add(neighbor)) { // add returns false if already present
                    queue.add(neighbor);
                }
            }
		}
	}

	/// Gets the room in a specific direction
	/// @param direction The direction to get the room in
	/// @return The room in the specified direction
	public Room neighbour(ExitDirection direction) {
		return neighbours.get(direction);
	}

	public Collection<Room> neighbours() {
		return neighbours.values();
	}

	/// Gets the number of neighbours this room has
	/// @return The number of neighbours
	public int getNeighbourCount() {
		// i do not trust the size of the neighbours map 
		// (i.e. this.neighbours.size())
        int count = 0;
        if (neighbour(ExitDirection.NORTH) != null) { count++; }
        if (neighbour(ExitDirection.SOUTH) != null) { count++; }
        if (neighbour(ExitDirection.EAST) != null) { count++; }
        if (neighbour(ExitDirection.WEST) != null) { count++; }
        return count;
	}

    /// Generates a random room layout
    /// @return The starting room
    public static Room generate() {
        Room mainRoom = new Room();
        int roomsRemaining = 16;
        Room currentRoom = mainRoom;

        while (roomsRemaining > 0) {
            ExitDirection direction = ExitDirection.random();

            if (currentRoom.neighbour(direction) == null) {
                Room newRoom = new Room();
                connect(currentRoom, direction, newRoom);
                currentRoom = newRoom;
                roomsRemaining--;
            } else {
                currentRoom = currentRoom.neighbour(direction);
            }
        }

        return mainRoom;
    }
}
