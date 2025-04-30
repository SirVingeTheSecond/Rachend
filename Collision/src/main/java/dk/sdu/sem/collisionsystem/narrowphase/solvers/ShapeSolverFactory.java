package dk.sdu.sem.collisionsystem.narrowphase.solvers;

import dk.sdu.sem.collision.data.ContactPoint;
import dk.sdu.sem.collision.shapes.*;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Factory that provides the solver for different shape combinations.
 */
public class ShapeSolverFactory {
	private static final GridShapeSolver gridShapeSolver = new GridShapeSolver();
	private static final GJKSolver gjkSolver = new GJKSolver();

	/**
	 * Solves collision between two shapes of any supported type.
	 *
	 * @param shapeA First shape
	 * @param posA Position of first shape
	 * @param shapeB Second shape
	 * @param posB Position of second shape
	 * @return ContactPoint if collision detected, null otherwise
	 */
	public static ContactPoint solve(ICollisionShape shapeA, Vector2D posA,
									 ICollisionShape shapeB, Vector2D posB) {
		// If both shapes are ConvexShape, use GJK algorithm
		if (shapeA instanceof ConvexShape a && shapeB instanceof ConvexShape b) {
			return gjkSolver.solve(a, posA, b, posB);
		}

		// Handle grid shape combinations since they're not convex
		else if (shapeA instanceof CircleShape && shapeB instanceof GridShape) {
			return gridShapeSolver.solveCircleGrid(
				(CircleShape)shapeA, posA,
				(GridShape)shapeB, posB
			);
		}
		else if (shapeA instanceof GridShape && shapeB instanceof CircleShape) {
			// Flip normal direction
			ContactPoint contact = gridShapeSolver.solveCircleGrid(
				(CircleShape)shapeB, posB,
				(GridShape)shapeA, posA
			);

			if (contact != null) {
				return new ContactPoint(
					contact.getPoint(),
					contact.getNormal().scale(-1), // Flip normal
					contact.getSeparation()
				);
			}
			return null;
		}
		else if (shapeA instanceof BoxShape && shapeB instanceof GridShape) {
			return gridShapeSolver.solveBoxGrid(
				(BoxShape)shapeA, posA,
				(GridShape)shapeB, posB
			);
		}
		else if (shapeA instanceof GridShape && shapeB instanceof BoxShape) {
			// Flip normal direction
			ContactPoint contact = gridShapeSolver.solveBoxGrid(
				(BoxShape)shapeB, posB,
				(GridShape)shapeA, posA
			);

			if (contact != null) {
				return new ContactPoint(
					contact.getPoint(),
					contact.getNormal().scale(-1), // Flip normal
					contact.getSeparation()
				);
			}
			return null;
		}

		// Unsupported shape combination
		return null;
	}
}