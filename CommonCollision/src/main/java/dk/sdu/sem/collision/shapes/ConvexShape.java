package dk.sdu.sem.collision.shapes;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Interface for convex shapes that can be used with GJK algorithm.
 */
public interface ConvexShape extends ICollisionShape {
	/**
	 * Returns the furthest point in the shape in a given direction.
	 * This is the support function required by the GJK algorithm.
	 *
	 * @param direction Direction vector to find the support point
	 * @return The furthest point in the shape in the given direction
	 */
	Vector2D getSupportPoint(Vector2D direction);

	/**
	 * Gets the center point of the shape.
	 * Used to generate initial search directions for GJK.
	 *
	 * @return The center point of the shape
	 */
	Vector2D getCenter();
}