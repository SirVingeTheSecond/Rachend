package dk.sdu.sem.collision.data;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Represents a contact point between two colliders.
 */
public class ContactPoint {
	private final Vector2D point;
	private final Vector2D normal;
	private final float separation;

	/**
	 * Creates a new contact point.
	 *
	 * @param point The world position of the contact
	 * @param normal The surface normal at the contact point
	 * @param separation The distance between the colliders at the contact point (negative for penetration)
	 */
	public ContactPoint(Vector2D point, Vector2D normal, float separation) {
		this.point = point;
		this.normal = normal;
		this.separation = separation;
	}

	/**
	 * Gets the world position of the contact.
	 */
	public Vector2D getPoint() {
		return point;
	}

	/**
	 * Gets the surface normal at the contact point.
	 */
	public Vector2D getNormal() {
		return normal;
	}

	/**
	 * Gets the distance between the colliders at the contact point.
	 * Negative values indicate penetration.
	 */
	public float getSeparation() {
		return separation;
	}
}