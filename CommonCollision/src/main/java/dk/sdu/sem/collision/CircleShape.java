package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Circle collision shape.
 * Good for characters and other entities.
 */
public class CircleShape implements ICollisionShape {
	private final Vector2D center;
	private final float radius;

	/**
	 * Creates a new circle shape.
	 *
	 * @param center Center point of the circle
	 * @param radius Radius of the circle
	 */
	public CircleShape(Vector2D center, float radius) {
		this.center = center;
		this.radius = radius;
	}

	/**
	 * Gets the center of the circle.
	 *
	 * @return The center point
	 */
	public Vector2D getCenter() {
		return center;
	}

	/**
	 * Gets the radius of the circle.
	 *
	 * @return The radius
	 */
	public float getRadius() {
		return radius;
	}

	@Override
	public boolean intersects(ICollisionShape other) {
		if (other instanceof CircleShape) {
			CircleShape otherCircle = (CircleShape) other;
			float distanceSquared = center.subtract(otherCircle.center).magnitudeSquared();
			float radiusSum = radius + otherCircle.radius;
			return distanceSquared <= radiusSum * radiusSum;
		} else if (other instanceof RectangleShape) {
			RectangleShape rect = (RectangleShape) other;
			return rect.intersects(this);
		}
		return false;
	}

	@Override
	public boolean contains(Vector2D point) {
		float distanceSquared = center.subtract(point).magnitudeSquared();
		return distanceSquared <= radius * radius;
	}

	/**
	 * Creates a new circle shape at a different position.
	 *
	 * @param newCenter The new center point
	 * @return A new circle shape
	 */
	public CircleShape withCenter(Vector2D newCenter) {
		return new CircleShape(newCenter, radius);
	}
}