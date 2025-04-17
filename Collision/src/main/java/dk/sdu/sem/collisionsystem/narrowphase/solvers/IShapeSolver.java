package dk.sdu.sem.collisionsystem.narrowphase.solvers;

import dk.sdu.sem.collision.data.ContactPoint;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Base interface for shape-specific collision solvers
 */
interface IShapeSolver<T1, T2> {
	/**
	 * Tests for collision between two shapes and generates contact information.
	 *
	 * @param shapeA First shape
	 * @param posA Position of first shape
	 * @param shapeB Second shape
	 * @param posB Position of second shape
	 * @return ContactPoint if collision detected, null otherwise
	 */
	ContactPoint solve(T1 shapeA, Vector2D posA, T2 shapeB, Vector2D posB);
}