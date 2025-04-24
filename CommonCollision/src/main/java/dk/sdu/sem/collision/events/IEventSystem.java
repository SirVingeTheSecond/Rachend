package dk.sdu.sem.collision.events;

/**
 * Service Provider Interface for the event system.
 * Provides a way to publish and subscribe to events.
 */
public interface IEventSystem {
	/**
	 * Subscribes a listener to an event type.
	 *
	 * @param eventType The event class
	 * @param listener The listener to subscribe
	 * @param <T> The event type
	 */
	<T> void subscribe(Class<T> eventType, IEventListener<T> listener);

	/**
	 * Unsubscribes a listener from an event type.
	 *
	 * @param eventType The event class
	 * @param listener The listener to unsubscribe
	 * @param <T> The event type
	 */
	<T> void unsubscribe(Class<T> eventType, IEventListener<T> listener);

	/**
	 * Publishes an event to all subscribed listeners.
	 *
	 * @param event The event to publish
	 * @param <T> The event type
	 */
	<T> void publish(T event);

	/**
	 * Clears all listeners.
	 * Should be called when switching scenes.
	 */
	void clear();
}
