package dk.sdu.sem.commonlevel.events;

import dk.sdu.sem.commonlevel.Direction;
import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.events.IEvent;

public class TransitionStartEvent implements IEvent {
	private final Entity fromRoom;
	private final Entity toRoom;
	private final Direction direction;
	private final Entity transitionEntity;


	public TransitionStartEvent(Entity fromRoom, Entity toRoom, Direction direction, Entity transitionEntity) {
		this.fromRoom = fromRoom;
		this.toRoom = toRoom;
		this.direction = direction;
		this.transitionEntity = transitionEntity;
	}

	public Entity getFromRoom() {
		return fromRoom;
	}

	public Entity getToRoom() {
		return toRoom;
	}

	public Direction getDirection() {
		return direction;
	}

	public Entity getTransitionEntity() {
		return transitionEntity;
	}
}
