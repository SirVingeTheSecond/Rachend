package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Represents an Axis-Aligned Bounding Box used for spatial partitioning.
 */
public class AABB {
	private final float minX, minY, maxX, maxY;

	public AABB(float minX, float minY, float maxX, float maxY) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
	}

	/**
	 * Creates an AABB from a center point and half dimensions.
	 */
	public static AABB fromCenterAndSize(Vector2D center, float halfWidth, float halfHeight) {
		return new AABB(
			center.getX() - halfWidth,
			center.getY() - halfHeight,
			center.getX() + halfWidth,
			center.getY() + halfHeight
		);
	}

	/**
	 * Checks if this AABB contains a point.
	 */
	public boolean contains(Vector2D point) {
		return point.getX() >= minX && point.getX() <= maxX &&
			point.getY() >= minY && point.getY() <= maxY;
	}

	/**
	 * Checks if this AABB contains another AABB.
	 */
	public boolean contains(AABB other) {
		return other.minX >= minX && other.maxX <= maxX &&
			other.minY >= minY && other.maxY <= maxY;
	}

	/**
	 * Checks if this AABB intersects with another AABB.
	 */
	public boolean intersects(AABB other) {
		return !(other.minX > maxX || other.maxX < minX ||
			other.minY > maxY || other.maxY < minY);
	}

	/**
	 * Gets the center point of this AABB.
	 */
	public Vector2D getCenter() {
		return new Vector2D((minX + maxX) * 0.5f, (minY + maxY) * 0.5f);
	}

	// Getters
	public float getMinX() { return minX; }
	public float getMinY() { return minY; }
	public float getMaxX() { return maxX; }
	public float getMaxY() { return maxY; }
	public float getWidth() { return maxX - minX; }
	public float getHeight() { return maxY - minY; }

	/**
	 * Splits this AABB into four equal quadrants.
	 */
	public AABB[] split() {
		Vector2D center = getCenter();
		float centerX = center.getX();
		float centerY = center.getY();

		return new AABB[] {
			new AABB(minX, minY, centerX, centerY),          // Bottom left
			new AABB(centerX, minY, maxX, centerY),          // Bottom right
			new AABB(minX, centerY, centerX, maxY),          // Top left
			new AABB(centerX, centerY, maxX, maxY)           // Top right
		};
	}
}