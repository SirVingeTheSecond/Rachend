package dk.sdu.sem.collisionsystem.systems;

import dk.sdu.sem.collision.ICollisionListener;
import dk.sdu.sem.collision.ITriggerListener;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.events.*;
import dk.sdu.sem.collisionsystem.events.EventSystem;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.gamesystem.services.IStart;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * System for connecting collision and trigger listener components to the event system.
 */
public class CollisionListenerSystem implements IUpdate, IStart {
	private static final boolean DEBUG = true;

	// Event system from service loader
	private final IEventSystem eventSystem;

	// Track registered listeners to avoid duplicates and enable unsubscription
	private final Map<Entity, Map<IComponent, Set<ListenerRegistration>>> registeredListeners = new HashMap<>();

	// Track processed entities to avoid redundant processing
	private final Set<Entity> processedEntities = new HashSet<>();

	/**
	 * Creates a new collision listener system.
	 */
	public CollisionListenerSystem() {
		this.eventSystem = EventSystem.getInstance();
	}

	@Override
	public void start() {
		System.out.println("CollisionListenerSystem.start() called");
		// Initial processing of all existing entities
		Scene activeScene = Scene.getActiveScene();
		if (activeScene != null) {
			System.out.println("Processing " + activeScene.getEntities().size() + " entities");
			for (Entity entity : activeScene.getEntities()) {
				processEntity(entity);
			}
		}
	}

	@Override
	public void update() {
		Scene activeScene = Scene.getActiveScene();
		if (activeScene == null) return;

		// Process new entities that haven't been processed yet
		Set<Entity> entities = activeScene.getEntities();
		for (Entity entity : entities) {
			if (!processedEntities.contains(entity)) {
				processEntity(entity);
			}
		}

		// Clean up processed entities that are no longer in the scene
		processedEntities.removeIf(entity -> entity.getScene() == null);
	}

	/**
	 * Processes an entity's components to find and register listeners.
	 */
	private void processEntity(Entity entity) {
		if (entity == null) return;

		//System.out.println("Processing entity: " + entity.getID());
		// Mark as processed
		processedEntities.add(entity);

		// Register collision listeners if entity has a collider
		if (entity.hasComponent(ColliderComponent.class)) {
			//System.out.println("Entity has collider, registering listeners");
			// Find and register all collision listeners
			registerCollisionListeners(entity);

			// Find and register all trigger listeners
			registerTriggerListeners(entity);
		}
	}

	/**
	 * Registers all components that implement ICollisionListener.
	 */
	private void registerCollisionListeners(Entity entity) {
		for (IComponent component : entity.getAllComponents()) {
			if (component instanceof ICollisionListener listener) {
				registerCollisionListener(entity, component, listener);
			}
		}
	}

	/**
	 * Registers all components that implement ITriggerListener.
	 */
	private void registerTriggerListeners(Entity entity) {
		for (IComponent component : entity.getAllComponents()) {
			if (component instanceof ITriggerListener listener) {
				registerTriggerListener(entity, component, listener);
			}
		}
	}

	/**
	 * Registers a collision listener with the event system.
	 */
	private void registerCollisionListener(Entity entity, IComponent component, ICollisionListener listener) {
		// Create event listeners for each collision event type
		IEventListener<CollisionEnterEvent> enterListener = event -> {
			System.out.println("Collision event received for entity: " + event.getEntity().getID()); // Add this debug line
			if (event.getEntity() == entity) {
				listener.onCollisionEnter(event);
			}
		};

		IEventListener<CollisionStayEvent> stayListener = event -> {
			if (event.getEntity() == entity) {
				listener.onCollisionStay(event);
			}
		};

		IEventListener<CollisionExitEvent> exitListener = event -> {
			if (event.getEntity() == entity) {
				listener.onCollisionExit(event);
			}
		};

		// Subscribe to event system
		eventSystem.subscribe(CollisionEnterEvent.class, enterListener);
		eventSystem.subscribe(CollisionStayEvent.class, stayListener);
		eventSystem.subscribe(CollisionExitEvent.class, exitListener);

		// Record registrations
		Map<IComponent, Set<ListenerRegistration>> entityListeners =
			registeredListeners.computeIfAbsent(entity, k -> new HashMap<>());
		Set<ListenerRegistration> registrations =
			entityListeners.computeIfAbsent(component, k -> new HashSet<>());

		registrations.add(new ListenerRegistration(CollisionEnterEvent.class, enterListener));
		registrations.add(new ListenerRegistration(CollisionStayEvent.class, stayListener));
		registrations.add(new ListenerRegistration(CollisionExitEvent.class, exitListener));

		if (DEBUG) {
			System.out.println("Registered collision listener: " + component.getClass().getSimpleName()
				+ " for entity: " + entity.getID());
		}
	}

	/**
	 * Registers a trigger listener with the event system.
	 */
	private void registerTriggerListener(Entity entity, IComponent component, ITriggerListener listener) {
		// Create event listeners for each trigger event type
		IEventListener<TriggerEnterEvent> enterListener = event -> {
			//System.out.println("Trigger event received for entity: " + event.getEntity().getID()); // Add this debug line
			if (event.getEntity() == entity) {
				listener.onTriggerEnter(event);
			}
		};

		IEventListener<TriggerStayEvent> stayListener = event -> {
			if (event.getEntity() == entity) {
				listener.onTriggerStay(event);
			}
		};

		IEventListener<TriggerExitEvent> exitListener = event -> {
			if (event.getEntity() == entity) {
				listener.onTriggerExit(event);
			}
		};

		// Subscribe to event system
		eventSystem.subscribe(TriggerEnterEvent.class, enterListener);
		eventSystem.subscribe(TriggerStayEvent.class, stayListener);
		eventSystem.subscribe(TriggerExitEvent.class, exitListener);

		// Record registrations
		Map<IComponent, Set<ListenerRegistration>> entityListeners =
			registeredListeners.computeIfAbsent(entity, k -> new HashMap<>());
		Set<ListenerRegistration> registrations =
			entityListeners.computeIfAbsent(component, k -> new HashSet<>());

		registrations.add(new ListenerRegistration(TriggerEnterEvent.class, enterListener));
		registrations.add(new ListenerRegistration(TriggerStayEvent.class, stayListener));
		registrations.add(new ListenerRegistration(TriggerExitEvent.class, exitListener));

		if (DEBUG) {
			System.out.println("Registered trigger listener: " + component.getClass().getSimpleName()
				+ " for entity: " + entity.getID());
		}
	}

	/**
	 * Unregisters a component's listeners from an entity.
	 * Call this when a component is removed or an entity is destroyed.
	 */
	public void unregisterComponent(Entity entity, IComponent component) {
		Map<IComponent, Set<ListenerRegistration>> entityListeners = registeredListeners.get(entity);
		if (entityListeners == null) return;

		Set<ListenerRegistration> registrations = entityListeners.remove(component);
		if (registrations != null) {
			for (ListenerRegistration reg : registrations) {
				unsubscribeEvent(reg.eventType, reg.listener);
			}

			if (DEBUG) {
				System.out.println("Unregistered listeners for: " + component.getClass().getSimpleName()
					+ " from entity: " + entity.getID());
			}
		}

		// Clean up empty maps
		if (entityListeners.isEmpty()) {
			registeredListeners.remove(entity);
		}
	}

	/**
	 * Helper method to handle the generic type issues in unsubscribe.
	 */
	@SuppressWarnings("unchecked")
	private <T> void unsubscribeEvent(Class<?> eventType, IEventListener<?> listener) {
		eventSystem.unsubscribe((Class<T>) eventType, (IEventListener<T>) listener);
	}

	/**
	 * Helper class to track listener registrations.
	 */
	private static class ListenerRegistration {
		final Class<?> eventType;
		final IEventListener<?> listener;

		ListenerRegistration(Class<?> eventType, IEventListener<?> listener) {
			this.eventType = eventType;
			this.listener = listener;
		}
	}
}