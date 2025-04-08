package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Represents the result of a collision test between two entities.
 */
public class CollisionPair {
	private final ColliderNode nodeA;
	private final ColliderNode nodeB;
	private final ContactPoint contact;
	private final boolean isTrigger;

	public CollisionPair(ColliderNode nodeA, ColliderNode nodeB, ContactPoint contact, boolean isTrigger) {
		this.nodeA = nodeA;
		this.nodeB = nodeB;
		this.contact = contact;
		this.isTrigger = isTrigger;
	}

	public ColliderNode getNodeA() { return nodeA; }
	public ColliderNode getNodeB() { return nodeB; }
	public Entity getEntityA() { return nodeA.getEntity(); }
	public Entity getEntityB() { return nodeB.getEntity(); }
	public ContactPoint getContact() { return contact; }
	public boolean isTrigger() { return isTrigger; }

	/**
	 * Creates a unique collision pair identifier irrespective of entity order.
	 */
	public String getUniqueIdentifier() {
		String idA = nodeA.getEntity().getID();
		String idB = nodeB.getEntity().getID();
		// Order by ID to ensure consistency
		return idA.compareTo(idB) < 0 ? idA + "_" + idB : idB + "_" + idA;
	}
}