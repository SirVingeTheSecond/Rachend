package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Rectangle collision shape.
 * Good for tiles and rectangular objects.
 */
public class RectangleShape implements ICollisionShape {
	private final Vector2D position; // Top-left corner
	private final float width;
	private final float height;

	/**
	 * Creates a new rectangle shape.
	 *
	 * @param position Top-left corner position
	 * @param width Width of the rectangle
	 * @param height Height of the rectangle
	 */
	public RectangleShape(Vector2D position, float width, float height) {
		this.position = position;
		this.width = width;
		this.height = height;
	}

	/**
	 * Gets the position (top-left corner).
	 *
	 * @return The position
	 */
	public Vector2D getPosition() {
		return position;
	}

	/**
	 * Gets the width.
	 *
	 * @return The width
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * Gets the height.
	 *
	 * @return The height
	 */
	public float getHeight() {
		return height;
	}

	/**
	 * Gets the center point of the rectangle.
	 *
	 * @return The center point
	 */
	public Vector2D getCenter() {
		return new Vector2D(
			position.x() + width / 2,
			position.y() + height / 2
		);
	}

	@Override
	public boolean intersects(ICollisionShape other) {
		if (other instanceof RectangleShape) {
			RectangleShape otherRect = (RectangleShape) other;

			// AABB collision check
			return position.x() < otherRect.position.x() + otherRect.width &&
				position.x() + width > otherRect.position.x() &&
				position.y() < otherRect.position.y() + otherRect.height &&
				position.y() + height > otherRect.position.y();
		} else if (other instanceof CircleShape) {
			CircleShape circle = (CircleShape) other;

			// Find the closest point on the rectangle to the circle's center
			float closestX = Math.max(position.x(), Math.min(circle.getCenter().x(), position.x() + width));
			float closestY = Math.max(position.y(), Math.min(circle.getCenter().y(), position.y() + height));

			// Calculate distance between the closest point and circle center
			Vector2D closestPoint = new Vector2D(closestX, closestY);
			float distanceSquared = closestPoint.subtract(circle.getCenter()).magnitudeSquared();

			return distanceSquared <= circle.getRadius() * circle.getRadius();
		}
		return false;
	}

	@Override
	public boolean contains(Vector2D point) {
		return point.x() >= position.x() &&
			point.x() <= position.x() + width &&
			point.y() >= position.y() &&
			point.y() <= position.y() + height;
	}
}