package dk.sdu.sem.commonlevel.components;

import dk.sdu.sem.commonlevel.room.RoomType;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonlevel.Direction;

/**
 * Identifies an entity as a room with entrances/exits
 */
public class RoomComponent implements IComponent {
	private int roomId;
	private RoomType roomType;
	private Vector2D[] entrances = new Vector2D[4]; // NORTH, EAST, SOUTH, WEST
	private boolean[] doors = new boolean[4]; // Whether each direction has a door

	public RoomComponent(int roomId, RoomType roomType) {
		this.roomId = roomId;
		this.roomType = roomType;
	}

	public int getRoomId() { return roomId; }

	public RoomType getRoomType() { return roomType; }

	public Vector2D getEntrance(Direction direction) { return entrances[direction.getValue()]; }

	public void setEntrance(Direction direction, Vector2D position) { entrances[direction.getValue()] = position; }

	public boolean hasDoor(Direction direction) { return doors[direction.getValue()]; }

	public void setDoor(Direction direction, boolean hasDoor) { doors[direction.getValue()] = hasDoor; }
}