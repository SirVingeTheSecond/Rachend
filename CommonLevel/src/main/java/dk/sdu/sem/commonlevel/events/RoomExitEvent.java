package dk.sdu.sem.commonlevel.events;

import dk.sdu.sem.commonlevel.Direction;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonsystem.events.IEvent;

/**
 * Event fired when an entity exits a room boundary
 */
public class RoomExitEvent implements IEvent {
	private final Entity entity;
	private final Direction direction;
	private final Vector2D position;

	public RoomExitEvent(Entity entity, Direction direction, Vector2D position) {
		this.entity = entity;
		this.direction = direction;
		this.position = position;
	}

	// Getters
	public Entity getEntity() { return entity; }
	public Direction getDirection() { return direction; }
	public Vector2D getPosition() { return position; }
}