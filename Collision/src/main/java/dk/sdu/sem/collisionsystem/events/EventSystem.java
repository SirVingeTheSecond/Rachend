package dk.sdu.sem.collisionsystem.events;

import dk.sdu.sem.collision.events.IEventListener;
import dk.sdu.sem.collision.events.IEventSystem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the IEventSystem interface.
 * This is a hub for publishing and subscribing to events.
 */
public class EventSystem implements IEventSystem {
	// Map of event types to listeners
	private final Map<Class<?>, Set<IEventListener<?>>> listeners = new ConcurrentHashMap<>();

	public EventSystem() {
	}

	@Override
	public <T> void subscribe(Class<T> eventType, IEventListener<T> listener) {
		listeners.computeIfAbsent(eventType, k -> ConcurrentHashMap.newKeySet()).add(listener);
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
			for (IEventListener<?> listener : eventListeners) {
				((IEventListener<T>) listener).onEvent(event);
			}
		}
	}

	@Override
	public void clear() {
		listeners.clear();
	}
}