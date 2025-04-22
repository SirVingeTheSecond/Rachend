package dk.sdu.sem.collision.components;

import dk.sdu.sem.collision.data.CollisionPair;
import dk.sdu.sem.collision.data.TriggerPair;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Component that tracks the current collision state of an entity.
 */
public class CollisionStateComponent implements IComponent {
	private final Map<String, CollisionPair> activeCollisions = new HashMap<>();
	private final Map<String, TriggerPair> activeTriggers = new HashMap<>();

	/**
	 * Adds or updates an active collision.
	 *
	 * @param pair The collision pair
	 */
	public void addCollision(CollisionPair pair) {
		activeCollisions.put(pair.getId(), pair);
	}

	/**
	 * Removes an active collision.
	 *
	 * @param pairId The ID of the collision pair
	 * @return The removed collision pair, or null if not found
	 */
	public CollisionPair removeCollision(String pairId) {
		return activeCollisions.remove(pairId);
	}

	/**
	 * Checks if the entity has an active collision with the given ID.
	 *
	 * @param pairId The ID of the collision pair
	 * @return True if the collision is active, false otherwise
	 */
	public boolean hasCollision(String pairId) {
		return activeCollisions.containsKey(pairId);
	}

	/**
	 * Gets an active collision by ID.
	 *
	 * @param pairId The ID of the collision pair
	 * @return The collision pair, or null if not found
	 */
	public CollisionPair getCollision(String pairId) {
		return activeCollisions.get(pairId);
	}

	/**
	 * Gets all active collisions.
	 *
	 * @return An unmodifiable view of active collisions
	 */
	public Map<String, CollisionPair> getActiveCollisions() {
		return Collections.unmodifiableMap(activeCollisions);
	}

	/**
	 * Adds or updates an active trigger.
	 *
	 * @param pair The trigger pair
	 */
	public void addTrigger(TriggerPair pair) {
		activeTriggers.put(pair.getId(), pair);
	}

	/**
	 * Removes an active trigger.
	 *
	 * @param pairId The ID of the trigger pair
	 * @return The removed trigger pair, or null if not found
	 */
	public TriggerPair removeTrigger(String pairId) {
		return activeTriggers.remove(pairId);
	}

	/**
	 * Checks if the entity has an active trigger with the given ID.
	 *
	 * @param pairId The ID of the trigger pair
	 * @return True if the trigger is active, false otherwise
	 */
	public boolean hasTrigger(String pairId) {
		return activeTriggers.containsKey(pairId);
	}

	/**
	 * Gets an active trigger by ID.
	 *
	 * @param pairId The ID of the trigger pair
	 * @return The trigger pair, or null if not found
	 */
	public TriggerPair getTrigger(String pairId) {
		return activeTriggers.get(pairId);
	}

	/**
	 * Gets all active triggers.
	 *
	 * @return An unmodifiable view of active triggers
	 */
	public Map<String, TriggerPair> getActiveTriggers() {
		return Collections.unmodifiableMap(activeTriggers);
	}

	/**
	 * Clears all active collisions and triggers.
	 */
	public void clear() {
		activeCollisions.clear();
		activeTriggers.clear();
	}

	/**
	 * Removes any collisions or triggers involving the given entity.
	 *
	 * @param entity The entity to check against
	 */
	public void removeEntityCollisions(Entity entity) {
		activeCollisions.entrySet().removeIf(entry -> {
			CollisionPair pair = entry.getValue();
			return pair.getEntityA() == entity || pair.getEntityB() == entity;
		});

		activeTriggers.entrySet().removeIf(entry -> {
			TriggerPair pair = entry.getValue();
			return pair.getEntityA() == entity || pair.getEntityB() == entity;
		});
	}
}