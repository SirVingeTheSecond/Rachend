package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Represents a ray for raycasting and continuous collision detection.
 */
public class Ray {
	private final Vector2D origin;
	private final Vector2D direction;

	public Ray(Vector2D origin, Vector2D direction) {
		this.origin = origin;
		this.direction = direction.normalize();
	}

	public Vector2D getOrigin() {
		return origin;
	}

	public Vector2D getDirection() {
		return direction;
	}

	public Vector2D getPoint(float distance) {
		return origin.add(direction.scale(distance));
	}
}