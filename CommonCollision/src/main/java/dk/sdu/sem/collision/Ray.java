package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Represents a ray for raycasting.
 */
public class Ray {
	private static final float EPSILON = 0.0001f;

	private final Vector2D origin;
	private final Vector2D direction;

	/**
	 * Creates a ray with a normalized direction vector.
	 *
	 * @param origin The starting point of the ray
	 * @param direction The direction vector (will be normalized)
	 */
	public Ray(Vector2D origin, Vector2D direction) {
		this.origin = origin;

		// Normalize direction to ensure consistent behavior
		float mag = direction.magnitude();
		if (mag > EPSILON) {
			this.direction = direction.scale(1f / mag);
		} else {
			this.direction = new Vector2D(1, 0); // Default to right if zero
		}
	}

	public Vector2D getOrigin() {
		return origin;
	}

	public Vector2D getDirection() {
		return direction;
	}

	/**
	 * Gets a point along the ray at the specified distance.
	 *
	 * @param distance Distance from origin
	 * @return The point at the given distance
	 */
	public Vector2D getPoint(float distance) {
		return origin.add(direction.scale(distance));
	}
}