package dk.sdu.sem.commonsystem.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Event dispatcher for broadcasting events to registered listeners
 */
public class EventDispatcher {
	private static final EventDispatcher instance = new EventDispatcher();
	private final Map<Class<? extends IEvent>, Set<EventListener<?>>> listeners = new HashMap<>();

	public static EventDispatcher getInstance() {
		return instance;
	}

	public <T extends IEvent> void addListener(Class<T> eventClass, EventListener<T> listener) {
		listeners.computeIfAbsent(eventClass, k -> new HashSet<>()).add(listener);
	}

	public <T extends IEvent> void removeListener(Class<T> eventClass, EventListener<T> listener) {
		Set<EventListener<?>> eventListeners = listeners.get(eventClass);
		if (eventListeners != null) {
			eventListeners.remove(listener);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends IEvent> void dispatch(T event) {
		Set<EventListener<?>> eventListeners = listeners.get(event.getClass());
		if (eventListeners != null) {
			for (EventListener<?> listener : eventListeners) {
				((EventListener<T>) listener).onEvent(event);
			}
		}
	}
}