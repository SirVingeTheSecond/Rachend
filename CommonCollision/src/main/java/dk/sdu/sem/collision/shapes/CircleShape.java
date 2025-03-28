package dk.sdu.sem.collision.shapes;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Circle collision shape with consistent collision detection.
 */
public class CircleShape implements ICollisionShape {
	private final Vector2D center;
	private final float radius;
	private final float radiusSquared;

	// Small threshold for more consistent edge detection - think of it like skin-width
	private static final float COLLISION_THRESHOLD = 0.03f;

	/**
	 * Creates a new circle shape.
	 *
	 * @param center Center point of the circle
	 * @param radius Radius of the circle
	 */
	public CircleShape(Vector2D center, float radius) {
		this.center = center;
		this.radius = radius;
		this.radiusSquared = radius * radius; // Pre-calculated for performance
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
			// Rectangle intersection test with symmetric edge detection
			return testRectangleIntersection(rect);
		}
		return false;
	}

	/**
	 * Tests intersection with a rectangle.
	 *
	 * @param rect The rectangle to test against
	 * @return true if there is an intersection, false otherwise
	 */
	private boolean testRectangleIntersection(RectangleShape rect) {
		// Get rectangle position and dimensions
		Vector2D rectPos = rect.getPosition();
		float rectWidth = rect.getWidth();
		float rectHeight = rect.getHeight();

		// Calculate rectangle edges with a threshold for consistent detection
		float leftEdge = rectPos.getX() - COLLISION_THRESHOLD;
		float rightEdge = rectPos.getX() + rectWidth + COLLISION_THRESHOLD;
		float topEdge = rectPos.getY() - COLLISION_THRESHOLD;
		float bottomEdge = rectPos.getY() + rectHeight + COLLISION_THRESHOLD;

		// Find the closest point on the buffered rectangle to the circle's center
		float closestX = Math.max(leftEdge, Math.min(center.getX(), rightEdge));
		float closestY = Math.max(topEdge, Math.min(center.getY(), bottomEdge));

		// Calculate distance between closest point and circle center
		Vector2D closestPoint = new Vector2D(closestX, closestY);
		float distanceSquared = closestPoint.subtract(center).magnitudeSquared();

		// If the distance is less than or equal to the radius squared, they intersect
		return distanceSquared <= radiusSquared;
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