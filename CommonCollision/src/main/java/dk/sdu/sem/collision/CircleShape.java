package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Circle collision shape.
 */
public class CircleShape implements ICollisionShape {
	private final Vector2D center;
	private final float radius;
	private final float radiusSquared;

	/**
	 * Creates a new circle shape.
	 *
	 * @param center Center point of the circle
	 * @param radius Radius of the circle
	 */
	public CircleShape(Vector2D center, float radius) {
		this.center = center;
		this.radius = radius;
		this.radiusSquared = radius * radius; // Pre-calculated for that extra bit of performance :D
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
		if (other instanceof CircleShape otherCircle) {
			float distanceSquared = center.subtract(otherCircle.center).magnitudeSquared();
			float radiusSum = radius + otherCircle.radius;
			return distanceSquared <= radiusSum * radiusSum;
		} else if (other instanceof RectangleShape rect) {
			// Find the closest point on the rectangle to the circle's center
			float closestX = Math.max(rect.getPosition().getX(),
				Math.min(center.getX(), rect.getPosition().getX() + rect.getWidth()));
			float closestY = Math.max(rect.getPosition().getY(),
				Math.min(center.getY(), rect.getPosition().getY() + rect.getHeight()));

			// Calculate the distance squared between the closest point and circle center
			Vector2D closestPoint = new Vector2D(closestX, closestY);
			float distanceSquared = closestPoint.subtract(center).magnitudeSquared();

			// If the distance is less than the radius squared, they must be intersecting
			return distanceSquared <= radiusSquared;
		}
		return false;
	}

	@Override
	public boolean contains(Vector2D point) {
		float distanceSquared = center.subtract(point).magnitudeSquared();
		return distanceSquared <= radiusSquared;
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