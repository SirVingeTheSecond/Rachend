package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.ITriggerCollisionListener;
import dk.sdu.sem.collision.ITriggerEventSPI;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.gamesystem.services.IStart;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System that processes trigger events and dispatches them to registered handlers.
 * This system acts as a mediator between the collision system and event handlers.
 */
public class TriggerSystem implements ITriggerCollisionListener, IStart {
	private static final Logger LOGGER = Logger.getLogger(TriggerSystem.class.getName());
	private static final boolean DEBUG = false;

	// List of trigger event handlers loaded via ServiceLoader
	private final List<ITriggerEventSPI> eventHandlers = new ArrayList<>();

	@Override
	public void start() {
		if (DEBUG) LOGGER.info("Starting TriggerSystem...");

		try {
			// 1. Load all trigger event handlers
			loadEventHandlers();

			// 2. Register with CollisionSystem
			registerWithCollisionSystem();
		} catch (Exception e) {
			// Make sure we don't crash the application on startup
			LOGGER.log(Level.WARNING, "Error during TriggerSystem startup: " + e.getMessage(), e);
		}
	}

	/**
	 * Loads all event handlers from ServiceLoader
	 */
	private void loadEventHandlers() {
		ServiceLoader<ITriggerEventSPI> handlers = ServiceLoader.load(ITriggerEventSPI.class);
		handlers.forEach(handler -> {
			eventHandlers.add(handler);
			if (DEBUG) LOGGER.info("Loaded event handler: " + handler.getClass().getName());
		});

		if (DEBUG) LOGGER.info("Loaded " + eventHandlers.size() + " event handlers");
	}

	/**
	 * Registers this system with the CollisionSystem
	 */
	private void registerWithCollisionSystem() {
		ServiceLoader<ICollisionSPI> collisionSystems = ServiceLoader.load(ICollisionSPI.class);
		boolean registered = false;

		for (ICollisionSPI system : collisionSystems) {
			if (system instanceof CollisionSystem) {
				CollisionSystem collisionSystem = (CollisionSystem) system;
				try {
					collisionSystem.registerTriggerListener(this);
					if (DEBUG) LOGGER.info("Successfully registered with CollisionSystem");
					registered = true;
					break;
				} catch (Exception e) {
					LOGGER.log(Level.WARNING, "Error registering with CollisionSystem: " + e.getMessage(), e);
				}
			}
		}

		if (!registered) {
			LOGGER.warning("Could not register with any CollisionSystem instance");
		}
	}

	/**
	 * Called when a trigger collision occurs.
	 * This method is called by the CollisionSystem.
	 */
	@Override
	public void onTriggerCollision(Entity triggerEntity, Entity otherEntity, boolean isCollisionStart) {
		if (triggerEntity == null || otherEntity == null) {
			LOGGER.warning("Received trigger collision with null entity");
			return;
		}

		try {
			if (DEBUG) {
				LOGGER.info("Collision detected between:");
				LOGGER.info("  - Trigger entity: " + triggerEntity.getID());
				LOGGER.info("  - Other entity: " + otherEntity.getID());
				LOGGER.info("  - Is first frame: " + isCollisionStart);
			}

			// Determine event type (ENTER or STAY)
			ITriggerEventSPI.TriggerEventType eventType = isCollisionStart
				? ITriggerEventSPI.TriggerEventType.ENTER
				: ITriggerEventSPI.TriggerEventType.STAY;

			// Notify all handlers
			notifyHandlers(eventType, triggerEntity, otherEntity);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error processing collision: " + e.getMessage(), e);
		}
	}

	/**
	 * Called when a trigger collision ends.
	 * This method is called by the CollisionSystem.
	 */
	@Override
	public void onTriggerCollisionEnd(Entity triggerEntity, Entity otherEntity) {
		if (triggerEntity == null || otherEntity == null) {
			LOGGER.warning("Received trigger collision end with null entity");
			return;
		}

		try {
			if (DEBUG) {
				LOGGER.info("Collision ended between:");
				LOGGER.info("  - Trigger entity: " + triggerEntity.getID());
				LOGGER.info("  - Other entity: " + otherEntity.getID());
			}

			// Notify all handlers of EXIT event
			notifyHandlers(ITriggerEventSPI.TriggerEventType.EXIT, triggerEntity, otherEntity);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error processing collision end: " + e.getMessage(), e);
		}
	}

	/**
	 * Notifies all event handlers of a trigger event.
	 */
	private void notifyHandlers(ITriggerEventSPI.TriggerEventType eventType,
								Entity triggerEntity, Entity otherEntity) {
		if (eventHandlers.isEmpty()) {
			LOGGER.warning("No event handlers registered, cannot process event");
			return;
		}

		if (DEBUG) {
			LOGGER.info("Notifying " + eventHandlers.size() +
				" handlers of " + eventType + " event");
		}

		for (ITriggerEventSPI handler : eventHandlers) {
			try {
				if (DEBUG) LOGGER.fine("Notifying handler: " + handler.getClass().getName());
				handler.processTriggerEvent(eventType, triggerEntity, otherEntity);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Error in event handler: " + e.getMessage(), e);
			}
		}
	}
}