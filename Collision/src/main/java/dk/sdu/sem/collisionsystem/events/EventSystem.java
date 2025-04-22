package dk.sdu.sem.collisionsystem.events;

import dk.sdu.sem.collision.events.IEventListener;
import dk.sdu.sem.collision.events.IEventSystem;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the IEventSystem interface.
 * This is for publishing and subscribing to events.
 */
public class EventSystem implements IEventSystem {
	private static final EventSystem instance = new EventSystem();

	// Map of event types to listeners
	private final Map<Class<?>, Set<IEventListener<?>>> listeners = new ConcurrentHashMap<>();

	private EventSystem() {
		System.out.println("Creating EventSystem singleton instance: " + System.identityHashCode(this));
	}

	public static EventSystem getInstance() {
		System.out.println("Getting EventSystem instance: " + System.identityHashCode(instance));
		return instance;
	}

	@Override
	public <T> void subscribe(Class<T> eventType, IEventListener<T> listener) {
		//System.out.println("Subscribing listener for event type: " + eventType.getSimpleName());
		listeners.computeIfAbsent(eventType, k -> ConcurrentHashMap.newKeySet()).add(listener);
		//System.out.println("Current listener count for " + eventType.getSimpleName() + ": " + listeners.get(eventType).size());
	}

	@Override
	public <T> void unsubscribe(Class<T> eventType, IEventListener<T> listener) {
		Set<IEventListener<?>> eventListeners = listeners.get(eventType);
		if (eventListeners != null) {
			eventListeners.remove(listener);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> void publish(T event) {
		Set<IEventListener<?>> eventListeners = listeners.get(event.getClass());
		if (eventListeners != null) {
			//System.out.println("Publishing event: " + event.getClass().getSimpleName());
			//System.out.println("  Number of listeners: " + eventListeners.size());
			for (IEventListener<?> listener : eventListeners) {
				//System.out.println("  Calling listener: " + listener.getClass().getName());
				((IEventListener<T>) listener).onEvent(event);
			}
		} else {
			System.out.println("No listeners registered for event type: " + event.getClass().getSimpleName());
		}
	}

	@Override
	public void clear() {
		listeners.clear();
	}
}