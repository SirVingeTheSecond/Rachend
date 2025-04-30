package dk.sdu.sem.collision.shapes;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Circle-shaped collision shape.
 */
public class CircleShape implements ConvexShape {
	private float radius;

	public CircleShape() {
	}

	/**
	 * Creates a new circle shape.
	 *
	 * @param radius The radius of the circle
	 */
	public CircleShape(float radius) {
		this.radius = radius;
	}

	/**
	 * Gets the radius of the circle.
	 */
	public float getRadius() {
		return radius;
	}

	/**
	 * Sets the radius of the circle.
	 */
	public void setRadius(float radius) {
		this.radius = radius;
	}

	@Override
	public boolean intersects(ICollisionShape other) {
		// Default implementation, actual collision detection is handled by the collision system
		return false;
	}

	@Override
	public boolean contains(Vector2D point) {
		// The point is relative to the circle center, which is handled by the collision system
		return point.magnitudeSquared() <= radius * radius;
	}

	@Override
	public Bounds getBounds() {
		return new Bounds(-radius, -radius, radius * 2, radius * 2);
	}

	@Override
	public Vector2D getSupportPoint(Vector2D direction) {
		// For a circle, the support point is always in the direction of the ray
		// scaled by the radius, from the center point
		if (direction.magnitudeSquared() < 0.0001f) {
			return new Vector2D(radius, 0); // Default direction if vector is near-zero
		}
		return direction.normalize().scale(radius);
	}

	@Override
	public Vector2D getCenter() {
		return new Vector2D(0, 0); // Center point is at origin in local space
	}
}