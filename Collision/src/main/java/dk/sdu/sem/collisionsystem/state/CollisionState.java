package dk.sdu.sem.collisionsystem.state;

import dk.sdu.sem.collision.data.CollisionPair;
import dk.sdu.sem.collision.data.TriggerPair;
import dk.sdu.sem.commonsystem.Entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains the current collision state for the entire scene.
 * This is a snapshot of the collision state accessible during a single physics update.
 */
public class CollisionState {
	// Current collisions in the scene
	private final Set<CollisionPair> currentCollisions = Collections.newSetFromMap(new ConcurrentHashMap<>());

	// Current triggers in the scene
	private final Set<TriggerPair> currentTriggers = Collections.newSetFromMap(new ConcurrentHashMap<>());

	// Entities that need collision state cleanup (recently destroyed)
	private final Set<Entity> entitiesToCleanup = Collections.newSetFromMap(new ConcurrentHashMap<>());

	// Flag to indicate collision update is in progress
	private volatile boolean isUpdating = false;

	/**
	 * Gets a read-only view of current collisions.
	 */
	public Set<CollisionPair> getCurrentCollisions() {
		return Collections.unmodifiableSet(currentCollisions);
	}

	/**
	 * Gets a read-only view of current triggers.
	 */
	public Set<TriggerPair> getCurrentTriggers() {
		return Collections.unmodifiableSet(currentTriggers);
	}

	/**
	 * Sets the current collisions.
	 * Should only be called by the collision system during physics update.
	 */
	public void setCurrentCollisions(Set<CollisionPair> collisions) {
		if (!isUpdating) {
			throw new IllegalStateException("Cannot update collision state outside of collision update");
		}
		currentCollisions.clear();
		currentCollisions.addAll(collisions);
	}

	/**
	 * Sets the current triggers.
	 * Should only be called by the collision system during physics update.
	 */
	public void setCurrentTriggers(Set<TriggerPair> triggers) {
		if (!isUpdating) {
			throw new IllegalStateException("Cannot update collision state outside of collision update");
		}
		currentTriggers.clear();
		currentTriggers.addAll(triggers);
	}

	/**
	 * Marks the collision update as started.
	 * Should only be called by the collision system.
	 */
	public void beginUpdate() {
		isUpdating = true;
	}

	/**
	 * Marks the collision update as completed.
	 * Should only be called by the collision system.
	 */
	public void endUpdate() {
		isUpdating = false;
	}

	/**
	 * Adds an entity to the cleanup list.
	 * These entities will have their collision state cleaned up on the next physics update.
	 */
	public void markForCleanup(Entity entity) {
		entitiesToCleanup.add(entity);
	}

	/**
	 * Gets and clears the list of entities to clean up.
	 * Should only be called by the collision system during physics update.
	 */
	public Set<Entity> getAndClearCleanupEntities() {
		if (!isUpdating) {
			throw new IllegalStateException("Cannot access cleanup entities outside of collision update");
		}

		Set<Entity> entities = new HashSet<>(entitiesToCleanup);
		entitiesToCleanup.clear();
		return entities;
	}

	/**
	 * Checks if the collision update is in progress.
	 */
	public boolean isUpdating() {
		return isUpdating;
	}

	/**
	 * Clears the collision state.
	 * Should only be called when switching scenes or shutting down.
	 */
	public void clear() {
		currentCollisions.clear();
		currentTriggers.clear();
		entitiesToCleanup.clear();
	}
}