package dk.sdu.sem.commonsystem.events;

/**
 * Interface for event listeners
 */
public interface EventListener<T extends IEvent> {
	void onEvent(T event);
}