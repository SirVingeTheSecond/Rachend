package dk.sdu.sem.collision.data;

import dk.sdu.sem.commonsystem.Entity;

/**
 * Represents a pair of entities where at least one has a trigger collider.
 * Used internally by the physics system.
 */
public class TriggerPair {
	private final Entity entityA;
	private final Entity entityB;
	private final String id;

	/**
	 * Creates a new trigger pair from a collision pair.
	 *
	 * @param collisionPair The collision pair to convert
	 */
	public TriggerPair(CollisionPair collisionPair) {
		this.entityA = collisionPair.getEntityA();
		this.entityB = collisionPair.getEntityB();
		this.id = collisionPair.getId();
	}

	/**
	 * Creates a new trigger pair.
	 *
	 * @param entityA The first entity
	 * @param entityB The second entity
	 */
	public TriggerPair(Entity entityA, Entity entityB) {
		this.entityA = entityA;
		this.entityB = entityB;

		// Create a unique ID for this trigger pair
		String idA = entityA.getID();
		String idB = entityB.getID();
		this.id = idA.compareTo(idB) < 0 ? idA + "_" + idB : idB + "_" + idA;
	}

	/**
	 * Gets the ID of this trigger pair.
	 * This is a unique identifier based on the entity IDs.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets the first entity in this trigger.
	 */
	public Entity getEntityA() {
		return entityA;
	}

	/**
	 * Gets the second entity in this trigger.
	 */
	public Entity getEntityB() {
		return entityB;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TriggerPair pair = (TriggerPair) o;
		return id.equals(pair.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}