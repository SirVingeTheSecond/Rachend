package dk.sdu.sem.commonlevel.events;

import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.events.IEvent;

/**
 * Event fired when a room transition completes
 */
public class TransitionCompleteEvent implements IEvent {
	private final Entity newRoom;
	private final Entity transitionEntity;

	public TransitionCompleteEvent(Entity newRoom, Entity transitionEntity) {
		this.newRoom = newRoom;
		this.transitionEntity = transitionEntity;
	}

	public Entity getNewRoom() { return newRoom; }

	public Entity getTransitionEntity() { return transitionEntity; }
}