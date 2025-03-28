package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.*;
import dk.sdu.sem.commonsystem.*;
import dk.sdu.sem.gamesystem.services.IStart;

import java.util.*;
import java.util.ServiceLoader;

/**
 * System that processes trigger events and dispatches them to registered handlers.
 * Receives collision events from the CollisionSystem and converts them to OnTriggerEnter/Stay/Exit events.
 */
public class TriggerSystem implements ITriggerCollisionListener, IStart {
	// Cache of trigger event SPIs loaded via ServiceLoader
	private final List<ITriggerEventSPI> triggerEventHandlers = new ArrayList<>();

	// Performance optimization for filtering event dispatches
	private final Map<ITriggerEventSPI, Set<Class<? extends IComponent>>> handlerInterestedComponents = new HashMap<>();

	// Track active trigger-entity pairs for STAY events
	private final Set<Pair<Entity, Entity>> activeTriggerPairs = new HashSet<>();

	/**
	 * Initializes the TriggerSystem and registers it with the CollisionSystem.
	 */
	@Override
	public void start() {
		// Load all trigger event handlers via ServiceLoader
		ServiceLoader.load(ITriggerEventSPI.class).forEach(handler -> {
			triggerEventHandlers.add(handler);

			// If handler implements optional interface to declare interests
			if (handler instanceof ITriggerEventInterests) {
				handlerInterestedComponents.put(
					handler,
					((ITriggerEventInterests)handler).getComponentsOfInterest()
				);
			}
		});

		// Register as a trigger listener with the CollisionSystem
		// We need to get the CollisionSystem instance
		try {
			// Find the CollisionSystem in the active services
			ServiceLoader<ICollisionSPI> collisionServices = ServiceLoader.load(ICollisionSPI.class);
			for (ICollisionSPI service : collisionServices) {
				if (service instanceof CollisionSystem) {
					((CollisionSystem) service).registerTriggerListener(this);
					break;
				}
			}
		} catch (Exception e) {
			System.err.println("Failed to register TriggerSystem with CollisionSystem: " + e.getMessage());
		}

		System.out.println("TriggerSystem loaded " + triggerEventHandlers.size() + " trigger event handlers");
	}

	/**
	 * Called by CollisionSystem when a trigger collision occurs.
	 */
	@Override
	public void onTriggerCollision(Entity triggerEntity, Entity otherEntity, boolean isCollisionStart) {
		// Create a pair to track this trigger interaction
		Pair<Entity, Entity> pair = Pair.of(triggerEntity, otherEntity);

		// Track active pairs for STAY events
		activeTriggerPairs.add(pair);

		// Determine the event type
		ITriggerEventSPI.TriggerEventType eventType = isCollisionStart
			? ITriggerEventSPI.TriggerEventType.ENTER
			: ITriggerEventSPI.TriggerEventType.STAY;

		// Dispatch event to handlers
		fireTriggerEvent(eventType, triggerEntity, otherEntity);
	}

	/**
	 * Called by CollisionSystem when a trigger collision ends.
	 */
	@Override
	public void onTriggerCollisionEnd(Entity triggerEntity, Entity otherEntity) {
		// Remove from active pairs
		Pair<Entity, Entity> pair = Pair.of(triggerEntity, otherEntity);
		activeTriggerPairs.remove(pair);

		// Fire EXIT event
		fireTriggerEvent(ITriggerEventSPI.TriggerEventType.EXIT, triggerEntity, otherEntity);
	}

	/**
	 * Dispatches a trigger event to all registered handlers with appropriate filtering.
	 */
	private void fireTriggerEvent(ITriggerEventSPI.TriggerEventType eventType,
								  Entity triggerEntity, Entity otherEntity) {
		for (ITriggerEventSPI handler : triggerEventHandlers) {
			// Check if this handler has declared specific interests
			Set<Class<? extends IComponent>> interests = handlerInterestedComponents.get(handler);

			// If handler has interests, check if either entity has an interesting component
			if (interests != null) {
				boolean hasInterestingComponent = false;

				// Check if either entity has any of the interesting components
				for (Class<? extends IComponent> componentClass : interests) {
					if (triggerEntity.hasComponent(componentClass) ||
						otherEntity.hasComponent(componentClass)) {
						hasInterestingComponent = true;
						break;
					}
				}

				if (!hasInterestingComponent) {
					continue;
				}
			}

			// Either no specific interests or an interesting component was found
			handler.processTriggerEvent(eventType, triggerEntity, otherEntity);
		}
	}

	/**
	 * Cleans up any trigger pairs involving the given entity.
	 * Should be called when an entity is removed from a scene.
	 */
	public void cleanupEntity(Entity entity) {
		// Remove any pairs that involve this entity
		activeTriggerPairs.removeIf(pair ->
			pair.getFirst() == entity || pair.getSecond() == entity);
	}
}