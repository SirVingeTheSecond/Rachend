package dk.sdu.sem.collisionsystem.narrowphase.solvers;

import dk.sdu.sem.collision.data.ContactPoint;
import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Interface for solvers that can detect collisions between specific shape types.
 *
 * @param <T> First shape type
 * @param <U> Second shape type
 */
public interface IShapeSolver<T extends ICollisionShape, U extends ICollisionShape> {

	/**
	 * Solves collision between two shapes.
	 *
	 * @param shapeA First shape
	 * @param posA Position of first shape
	 * @param shapeB Second shape
	 * @param posB Position of second shape
	 * @return ContactPoint if collision detected, null otherwise
	 */
	ContactPoint solve(T shapeA, Vector2D posA, U shapeB, Vector2D posB);
}