package dk.sdu.sem.collision.shapes;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Base interface for all collision shapes.
 */
public interface ICollisionShape {
	/**
	 * Tests if this shape intersects with another shape.
	 *
	 * @param other The other shape to test against
	 * @return true if shapes intersect, false otherwise
	 */
	boolean intersects(ICollisionShape other);

	/**
	 * Tests if this shape contains a point.
	 *
	 * @param point The point to test
	 * @return true if the shape contains the point, false otherwise
	 */
	boolean contains(Vector2D point);

	/**
	 * Gets the bounds of this shape.
	 *
	 * @return An axis-aligned bounding box representing the bounds of this shape
	 */
	Bounds getBounds();
}