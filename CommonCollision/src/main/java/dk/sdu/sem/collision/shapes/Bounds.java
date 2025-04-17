package dk.sdu.sem.collision.shapes;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Represents an axis-aligned bounding box.
 */
public class Bounds {
	private final float minX, minY, maxX, maxY;

	/**
	 * Creates a new bounds.
	 *
	 * @param minX The minimum X coordinate
	 * @param minY The minimum Y coordinate
	 * @param width The width of the bounds
	 * @param height The height of the bounds
	 */
	public Bounds(float minX, float minY, float width, float height) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = minX + width;
		this.maxY = minY + height;
	}

	/**
	 * Creates a new bounds from a center point and half size.
	 *
	 * @param center The center point
	 * @param halfWidth Half the width of the bounds
	 * @param halfHeight Half the height of the bounds
	 */
	public static Bounds fromCenter(Vector2D center, float halfWidth, float halfHeight) {
		return new Bounds(
			center.x() - halfWidth,
			center.y() - halfHeight,
			halfWidth * 2,
			halfHeight * 2
		);
	}

	/**
	 * Gets the minimum X coordinate.
	 */
	public float getMinX() {
		return minX;
	}

	/**
	 * Gets the minimum Y coordinate.
	 */
	public float getMinY() {
		return minY;
	}

	/**
	 * Gets the maximum X coordinate.
	 */
	public float getMaxX() {
		return maxX;
	}

	/**
	 * Gets the maximum Y coordinate.
	 */
	public float getMaxY() {
		return maxY;
	}

	/**
	 * Gets the width of the bounds.
	 */
	public float getWidth() {
		return maxX - minX;
	}

	/**
	 * Gets the height of the bounds.
	 */
	public float getHeight() {
		return maxY - minY;
	}

	/**
	 * Gets the center of the bounds.
	 */
	public Vector2D getCenter() {
		return new Vector2D((minX + maxX) / 2, (minY + maxY) / 2);
	}

	/**
	 * Checks if this bounds contains a point.
	 */
	public boolean contains(Vector2D point) {
		return point.x() >= minX && point.x() <= maxX &&
			point.y() >= minY && point.y() <= maxY;
	}

	/**
	 * Checks if this bounds intersects with another bounds.
	 */
	public boolean intersects(Bounds other) {
		return !(other.minX > maxX || other.maxX < minX ||
			other.minY > maxY || other.maxY < minY);
	}

	/**
	 * Gets the closest point on this bounds to a given point.
	 */
	public Vector2D closestPoint(Vector2D point) {
		float x = Math.max(minX, Math.min(point.x(), maxX));
		float y = Math.max(minY, Math.min(point.y(), maxY));
		return new Vector2D(x, y);
	}
}