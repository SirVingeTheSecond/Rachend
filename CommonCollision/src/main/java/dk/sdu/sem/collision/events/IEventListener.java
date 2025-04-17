package dk.sdu.sem.collision.events;

/**
 * Interface for event listeners.
 */
public interface IEventListener<T> {
	/**
	 * Called when an event is fired.
	 *
	 * @param event The event
	 */
	void onEvent(T event);
}
