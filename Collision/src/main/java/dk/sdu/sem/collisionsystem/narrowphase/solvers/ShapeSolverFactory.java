package dk.sdu.sem.collisionsystem.narrowphase.solvers;

import dk.sdu.sem.collision.ContactPoint;
import dk.sdu.sem.collision.shapes.BoxShape;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.collision.shapes.GridShape;
import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Factory that provides the appropriate solver for different shape combinations.
 */
public class ShapeSolverFactory {
	private static final CircleCircleSolver circleCircleSolver = new CircleCircleSolver();
	private static final CircleBoxSolver circleBoxSolver = new CircleBoxSolver();
	private static final BoxBoxSolver boxBoxSolver = new BoxBoxSolver();
	private static final GridShapeSolver gridShapeSolver = new GridShapeSolver();

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
		// Handle all shape combinations
		if (shapeA instanceof CircleShape && shapeB instanceof CircleShape) {
			return circleCircleSolver.solve(
				(CircleShape)shapeA, posA,
				(CircleShape)shapeB, posB
			);
		}
		else if (shapeA instanceof CircleShape && shapeB instanceof BoxShape) {
			return circleBoxSolver.solve(
				(CircleShape)shapeA, posA,
				(BoxShape)shapeB, posB
			);
		}
		else if (shapeA instanceof BoxShape && shapeB instanceof CircleShape) {
			// Flip normal direction since we're solving from B's perspective
			ContactPoint contact = circleBoxSolver.solve(
				(CircleShape)shapeB, posB,
				(BoxShape)shapeA, posA
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
		else if (shapeA instanceof BoxShape && shapeB instanceof BoxShape) {
			return boxBoxSolver.solve(
				(BoxShape)shapeA, posA,
				(BoxShape)shapeB, posB
			);
		}
		// Handle grid shape combinations
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
		else if (shapeA instanceof GridShape && shapeB instanceof GridShape) {
			return gridShapeSolver.solveGridGrid(
				(GridShape)shapeA, posA,
				(GridShape)shapeB, posB
			);
		}

		// Unsupported shape combination
		return null;
	}
}