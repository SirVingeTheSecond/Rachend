package dk.sdu.sem.collision.data;

import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.List;

/**
 * Contains information about a physical collision.
 * Similar to Unity's Collision class.
 */
public class Collision {
	private final Entity entity;
	private final ColliderComponent collider;
	private final List<ContactPoint> contacts;
	private final Vector2D relativeVelocity;

	/**
	 * Creates a new collision.
	 *
	 * @param entity The entity that was hit
	 * @param collider The collider that was hit
	 * @param contacts List of contact points
	 * @param relativeVelocity The relative velocity of the two colliding bodies
	 */
	public Collision(Entity entity, ColliderComponent collider, List<ContactPoint> contacts, Vector2D relativeVelocity) {
		this.entity = entity;
		this.collider = collider;
		this.contacts = contacts;
		this.relativeVelocity = relativeVelocity;
	}

	/**
	 * Gets the entity that was hit.
	 */
	public Entity getEntity() {
		return entity;
	}

	/**
	 * Gets the collider that was hit.
	 */
	public ColliderComponent getCollider() {
		return collider;
	}

	/**
	 * Gets the list of contact points.
	 */
	public List<ContactPoint> getContacts() {
		return contacts;
	}

	/**
	 * Gets the first contact point.
	 */
	public ContactPoint getContact() {
		return contacts.isEmpty() ? null : contacts.get(0);
	}

	/**
	 * Gets the relative velocity of the two colliding bodies.
	 */
	public Vector2D getRelativeVelocity() {
		return relativeVelocity;
	}

	/**
	 * Gets the total impulse applied by this collision.
	 */
	public float getImpulse() {
		// In a simple physics system, impulse can be estimated from velocity change
		return relativeVelocity.magnitude();
	}
}