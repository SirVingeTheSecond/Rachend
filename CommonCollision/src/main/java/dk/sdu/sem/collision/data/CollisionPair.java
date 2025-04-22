package dk.sdu.sem.collision.data;

import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.commonsystem.Entity;

/**
 * Represents a pair of colliding entities.
 * Used by the collision system.
 */
public class CollisionPair {
	private final Entity entityA;
	private final Entity entityB;
	private final ColliderComponent colliderA;
	private final ColliderComponent colliderB;
	private final ContactPoint contact;
	private final boolean isTrigger;
	private final String id;

	/**
	 * Creates a new collision pair.
	 *
	 * @param entityA The first entity
	 * @param entityB The second entity
	 * @param colliderA The first collider
	 * @param colliderB The second collider
	 * @param contact The contact point information
	 * @param isTrigger Whether this is a trigger collision
	 */
	public CollisionPair(Entity entityA, Entity entityB, ColliderComponent colliderA, ColliderComponent colliderB,
						 ContactPoint contact, boolean isTrigger) {
		this.entityA = entityA;
		this.entityB = entityB;
		this.colliderA = colliderA;
		this.colliderB = colliderB;
		this.contact = contact;
		this.isTrigger = isTrigger;

		// Create a unique ID for this collision pair
		String idA = entityA.getID();
		String idB = entityB.getID();
		this.id = idA.compareTo(idB) < 0 ? idA + "_" + idB : idB + "_" + idA;
	}

	/**
	 * Gets the ID of this collision pair.
	 * This is a unique identifier based on the entity IDs.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets the first entity in this collision.
	 */
	public Entity getEntityA() {
		return entityA;
	}

	/**
	 * Gets the second entity in this collision.
	 */
	public Entity getEntityB() {
		return entityB;
	}

	/**
	 * Gets the first collider in this collision.
	 */
	public ColliderComponent getColliderA() {
		return colliderA;
	}

	/**
	 * Gets the second collider in this collision.
	 */
	public ColliderComponent getColliderB() {
		return colliderB;
	}

	/**
	 * Gets the contact point information.
	 */
	public ContactPoint getContact() {
		return contact;
	}

	/**
	 * Checks if this is a trigger collision.
	 */
	public boolean isTrigger() {
		return isTrigger;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CollisionPair pair = (CollisionPair) o;
		return id.equals(pair.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}