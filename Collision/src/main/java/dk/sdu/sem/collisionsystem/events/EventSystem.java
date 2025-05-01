package dk.sdu.sem.collisionsystem.events;

import dk.sdu.sem.collision.events.IEventListener;
import dk.sdu.sem.collision.events.IEventSystem;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the IEventSystem interface.
 * This is for publishing and subscribing to events.
 */
public class EventSystem implements IEventSystem {
	private static final Logging LOGGER = Logging.createLogger("EventSystem", LoggingLevel.DEBUG);

	private static final EventSystem instance = new EventSystem();

	// Map of event types to listeners
	private final Map<Class<?>, Set<IEventListener<?>>> listeners = new ConcurrentHashMap<>();

	private EventSystem() {
		LOGGER.debug("Creating EventSystem singleton instance: " + System.identityHashCode(this));
	}

	public static EventSystem getInstance() {
		LOGGER.debug("Getting EventSystem instance: " + System.identityHashCode(instance));
		return instance;
	}

	@Override
	public <T> void subscribe(Class<T> eventType, IEventListener<T> listener) {
		//LOGGER.debug("Subscribing listener for event type: " + eventType.getSimpleName());
		listeners.computeIfAbsent(eventType, k -> ConcurrentHashMap.newKeySet()).add(listener);
		//LOGGER.debug("Current listener count for " + eventType.getSimpleName() + ": " + listeners.get(eventType).size());
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
			LOGGER.debug("Publishing event: " + event.getClass().getSimpleName());
			LOGGER.debug("  - Number of listeners: " + eventListeners.size());
			for (IEventListener<?> listener : eventListeners) {
				try {
					LOGGER.debug("  - Calling listener: " + listener.getClass().getName());
					((IEventListener<T>) listener).onEvent(event);
				} catch (Exception e) {
					// Log the exception but continue processing other listeners
					LOGGER.error("Exception in event listener: " + e.getMessage(), e);
				}
			}
		} else {
			LOGGER.debug("No listeners registered for event type: " + event.getClass().getSimpleName());
		}
	}

	@Override
	public void clear() {
		listeners.clear();
	}
}