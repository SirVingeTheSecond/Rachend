package dk.sdu.sem.collisionsystem.dispatching;

import dk.sdu.sem.collision.ITriggerListener;
import dk.sdu.sem.collisionsystem.CollisionPair;
import dk.sdu.sem.collisionsystem.TriggerEventType;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages and dispatches trigger events between entities.
 */
public class TriggerDispatcher {
	// Maps entity pairs to their active collision state
	private final Map<String, EntityPairState> activeTriggers = new HashMap<>();

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

			// Create unique identifier for this entity pair
			String pairId = pair.getUniqueIdentifier();
			currentFramePairs.add(pairId);

			// Check if this is a new or ongoing collision
			EntityPairState state = activeTriggers.get(pairId);
			if (state == null) {
				// New trigger collision - create state and dispatch Enter events
				state = new EntityPairState(entityA, entityB);
				activeTriggers.put(pairId, state);
				dispatchTriggerEnter(entityA, entityB);
			} else {
				// Ongoing collision - dispatch Stay events
				dispatchTriggerStay(entityA, entityB);
			}
		}

		// Check for ended collisions and dispatch Exit events
		Set<String> endedPairs = new HashSet<>(activeTriggers.keySet());
		endedPairs.removeAll(currentFramePairs);

		for (String pairId : endedPairs) {
			EntityPairState state = activeTriggers.remove(pairId);
			dispatchTriggerExit(state.entityA, state.entityB);
		}
	}

	/**
	 * Dispatches trigger enter events to both entities.
	 */
	private void dispatchTriggerEnter(Entity entityA, Entity entityB) {
		// Find and notify all ITriggerListener components on both entities
		notifyTriggerListeners(entityA, entityB, TriggerEventType.ENTER);
		notifyTriggerListeners(entityB, entityA, TriggerEventType.ENTER);
	}

	/**
	 * Dispatches trigger stay events to both entities.
	 */
	private void dispatchTriggerStay(Entity entityA, Entity entityB) {
		notifyTriggerListeners(entityA, entityB, TriggerEventType.STAY);
		notifyTriggerListeners(entityB, entityA, TriggerEventType.STAY);
	}

	/**
	 * Dispatches trigger exit events to both entities.
	 */
	private void dispatchTriggerExit(Entity entityA, Entity entityB) {
		notifyTriggerListeners(entityA, entityB, TriggerEventType.EXIT);
		notifyTriggerListeners(entityB, entityA, TriggerEventType.EXIT);
	}

	/**
	 * Notifies all trigger listeners on an entity about a trigger event.
	 */
	private void notifyTriggerListeners(Entity entity, Entity other, TriggerEventType eventType) {
		// Skip if either entity is invalid
		if (entity == null || other == null || entity.getScene() == null || other.getScene() == null) {
			return;
		}

		// Find all ITriggerListener components
		for (IComponent component : entity.getAllComponents()) {
			if (component instanceof ITriggerListener) {
				ITriggerListener listener = (ITriggerListener) component;

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