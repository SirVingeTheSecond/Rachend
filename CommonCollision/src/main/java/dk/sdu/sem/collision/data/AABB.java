package dk.sdu.sem.collision.data;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Represents an Axis-Aligned Bounding Box (AABB).
 * Used for spatial partitioning and basic collision tests.
 */
public class AABB {
	private final float minX, minY, maxX, maxY;

	/**
	 * Creates an AABB with the specified bounds.
	 *
	 * @param minX Minimum X coordinate
	 * @param minY Minimum Y coordinate
	 * @param maxX Maximum X coordinate
	 * @param maxY Maximum Y coordinate
	 */
	public AABB(float minX, float minY, float maxX, float maxY) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
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
	 * Gets the width of the AABB.
	 */
	public float getWidth() {
		return maxX - minX;
	}

	/**
	 * Gets the height of the AABB.
	 */
	public float getHeight() {
		return maxY - minY;
	}

	/**
	 * Checks if this AABB contains a point.
	 *
	 * @param point The point to test
	 * @return True if the point is inside this AABB, false otherwise
	 */
	public boolean contains(Vector2D point) {
		return point.x() >= minX && point.x() <= maxX &&
			point.y() >= minY && point.y() <= maxY;
	}

	/**
	 * Checks if this AABB contains another AABB entirely.
	 *
	 * @param other The other AABB to test
	 * @return True if this AABB contains the other, false otherwise
	 */
	public boolean contains(AABB other) {
		return other.minX >= minX && other.maxX <= maxX &&
			other.minY >= minY && other.maxY <= maxY;
	}

	/**
	 * Checks if this AABB intersects with another AABB.
	 *
	 * @param other The other AABB to test
	 * @return True if the AABBs intersect, false otherwise
	 */
	public boolean intersects(AABB other) {
		return !(other.minX > maxX || other.maxX < minX ||
			other.minY > maxY || other.maxY < minY);
	}

	/**
	 * Gets the center point of this AABB.
	 *
	 * @return The center point
	 */
	public Vector2D getCenter() {
		return new Vector2D((minX + maxX) * 0.5f, (minY + maxY) * 0.5f);
	}

	/**
	 * Splits this AABB into four equal quadrants.
	 *
	 * @return An array of four AABBs representing the quadrants
	 */
	public AABB[] split() {
		Vector2D center = getCenter();
		float centerX = center.x();
		float centerY = center.y();

		return new AABB[] {
			new AABB(minX, minY, centerX, centerY),     // Bottom left
			new AABB(centerX, minY, maxX, centerY),     // Bottom right
			new AABB(minX, centerY, centerX, maxY),     // Top left
			new AABB(centerX, centerY, maxX, maxY)      // Top right
		};
	}

	/**
	 * Creates an expanded copy of this AABB.
	 *
	 * @param amount Amount to expand in all directions
	 * @return The expanded AABB
	 */
	public AABB expand(float amount) {
		return new AABB(
			minX - amount,
			minY - amount,
			maxX + amount,
			maxY + amount
		);
	}

	@Override
	public String toString() {
		return String.format("AABB[%.2f, %.2f, %.2f, %.2f]", minX, minY, maxX, maxY);
	}
}