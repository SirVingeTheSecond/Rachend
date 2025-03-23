package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Interface for collision shapes that can be tested for intersection.
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
}