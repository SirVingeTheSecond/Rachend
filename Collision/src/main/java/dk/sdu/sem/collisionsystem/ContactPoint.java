package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Contact information for a collision.
 */
public class ContactPoint {
	private final Vector2D position;
	private final Vector2D normal;
	private final float penetrationDepth;

	public ContactPoint(Vector2D position, Vector2D normal, float penetrationDepth) {
		this.position = position;
		this.normal = normal;
		this.penetrationDepth = penetrationDepth;
	}

	public Vector2D getPosition() { return position; }
	public Vector2D getNormal() { return normal; }
	public float getPenetrationDepth() { return penetrationDepth; }
}
