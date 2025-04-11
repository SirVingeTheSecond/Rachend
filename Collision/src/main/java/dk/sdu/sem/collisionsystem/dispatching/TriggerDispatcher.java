package dk.sdu.sem.collisionsystem.dispatching;

import dk.sdu.sem.collision.CollisionPair;
import dk.sdu.sem.collision.ITriggerListener;
import dk.sdu.sem.collisionsystem.TriggerEventType;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages and dispatches trigger events between entities.
 */
public class TriggerDispatcher {
	// Maps entity pairs to their active collision state
	// Using ConcurrentHashMap for thread safety
	private final Map<String, EntityPairState> activeTriggers = new ConcurrentHashMap<>();

	/**
	 * Dispatches appropriate trigger events for collisions.
	 *
	 * @param collisions Set of collision pairs to process
	 */
	public void dispatchTriggerEvents(Set<CollisionPair> collisions) {
		// Create a set of pair identifiers for this frame
		Set<String> currentFramePairs = new HashSet<>();

		// Process all current collisions
		for (CollisionPair pair : collisions) {
			// Skip if not a trigger collision
			if (!pair.isTrigger()) {
				continue;
			}

			Entity entityA = pair.getEntityA();
			Entity entityB = pair.getEntityB();

			// Skip invalid entities
			if (entityA == null || entityB == null ||
				entityA.getScene() == null || entityB.getScene() == null) {
				continue;
			}

			// Create unique identifier for this entity pair
			String pairId = pair.getId();
			currentFramePairs.add(pairId);

			// Check if this is a new or ongoing collision
			EntityPairState state = activeTriggers.get(pairId);
			if (state == null) {
				// New trigger collision - create state and dispatch Enter events
				state = new EntityPairState(entityA, entityB);
				activeTriggers.put(pairId, state);
				dispatchTriggerEvent(entityA, entityB, TriggerEventType.ENTER);
				dispatchTriggerEvent(entityB, entityA, TriggerEventType.ENTER);
			} else {
				// Ongoing collision - dispatch Stay events
				dispatchTriggerEvent(entityA, entityB, TriggerEventType.STAY);
				dispatchTriggerEvent(entityB, entityA, TriggerEventType.STAY);
			}
		}

		// Check for ended collisions and dispatch Exit events
		Set<String> endedPairs = new HashSet<>(activeTriggers.keySet());
		endedPairs.removeAll(currentFramePairs);

		for (String pairId : endedPairs) {
			EntityPairState state = activeTriggers.remove(pairId);
			dispatchTriggerEvent(state.entityA, state.entityB, TriggerEventType.EXIT);
			dispatchTriggerEvent(state.entityB, state.entityA, TriggerEventType.EXIT);
		}
	}

	/**
	 * Dispatches a trigger event to all trigger listeners on an entity.
	 *
	 * @param entity The entity receiving the event
	 * @param other The other entity involved in the trigger
	 * @param eventType The type of trigger event (ENTER, STAY, EXIT)
	 */
	private void dispatchTriggerEvent(Entity entity, Entity other, TriggerEventType eventType) {
		// Skip if either entity is invalid
		if (entity == null || other == null || entity.getScene() == null || other.getScene() == null) {
			return;
		}

		// Find all ITriggerListener components
		for (IComponent component : entity.getAllComponents()) {
			if (component instanceof ITriggerListener listener) {
				switch (eventType) {
					case ENTER:
						listener.onTriggerEnter(other);
						break;
					case STAY:
						listener.onTriggerStay(other);
						break;
					case EXIT:
						listener.onTriggerExit(other);
						break;
				}
			}
		}
	}

	/**
	 * Removes all collision tracking for an entity.
	 * Used when entities are destroyed/removed from scene.
	 */
	public void removeEntityCollisions(Entity entity) {
		if (entity == null) {
			return;
		}

		// Find and remove all collision pairs involving this entity
		Set<String> pairsToRemove = new HashSet<>();

		for (Map.Entry<String, EntityPairState> entry : activeTriggers.entrySet()) {
			EntityPairState state = entry.getValue();
			if (state.entityA == entity || state.entityB == entity) {
				pairsToRemove.add(entry.getKey());
			}
		}

		// Remove the identified pairs
		for (String pairId : pairsToRemove) {
			activeTriggers.remove(pairId);
		}
	}

	/**
	 * Stores state information about a collision between two entities.
	 */
	private static class EntityPairState {
		final Entity entityA;
		final Entity entityB;

		EntityPairState(Entity entityA, Entity entityB) {
			this.entityA = entityA;
			this.entityB = entityB;
		}
	}
}