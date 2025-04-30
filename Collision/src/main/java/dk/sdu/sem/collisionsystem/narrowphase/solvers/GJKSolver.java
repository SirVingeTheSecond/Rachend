package dk.sdu.sem.collisionsystem.narrowphase.solvers;

import dk.sdu.sem.collision.data.ContactPoint;
import dk.sdu.sem.collision.shapes.ConvexShape;
import dk.sdu.sem.collisionsystem.narrowphase.gjk.EPA;
import dk.sdu.sem.collisionsystem.narrowphase.gjk.Simplex;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the Gilbert-Johnson-Keerthi (GJK) algorithm for collision detection
 * between convex shapes.
 */
public class GJKSolver implements IShapeSolver<ConvexShape, ConvexShape> {
	private static final int MAX_ITERATIONS = 20;
	private static final float EPSILON = 0.0001f;

	@Override
	public ContactPoint solve(ConvexShape shapeA, Vector2D posA, ConvexShape shapeB, Vector2D posB) {
		// Initial simplex
		Simplex simplex = new Simplex();

		// Initial direction is from center of shapeA to center of shapeB
		Vector2D center1 = posA.add(shapeA.getCenter());
		Vector2D center2 = posB.add(shapeB.getCenter());
		Vector2D[] direction = { center2.subtract(center1) };

		// If direction is zero, choose a default
		if (direction[0].magnitudeSquared() < EPSILON) {
			direction[0] = new Vector2D(1, 0);
		}

		// Get first support point
		Vector2D support = getSupport(shapeA, posA, shapeB, posB, direction[0]);
		simplex.add(support);

		// Negate direction for next iteration
		direction[0] = direction[0].scale(-1);

		// GJK iteration
		for (int i = 0; i < MAX_ITERATIONS; i++) {
			// Get a new support point in the direction
			support = getSupport(shapeA, posA, shapeB, posB, direction[0]);

			// Check if support point is on the correct side of origin
			if (support.dot(direction[0]) < 0) {
				// Shapes don't intersect
				return null;
			}

			// Add the point to simplex
			simplex.add(support);

			// Check if simplex contains origin
			if (simplex.containsOrigin(direction)) {
				// Shapes intersect, calculate contact information using EPA
				return runEPA(simplex, shapeA, posA, shapeB, posB);
			}
		}

		// If we reach here, GJK didn't converge
		return null;
	}

	/**
	 * Gets the support point in the Minkowski Difference of the two shapes.
	 *
	 * @param shapeA First shape
	 * @param posA Position of first shape
	 * @param shapeB Second shape
	 * @param posB Position of second shape
	 * @param direction Direction to search
	 * @return Support point in Minkowski Difference (A - B)
	 */
	private Vector2D getSupport(
		ConvexShape shapeA, Vector2D posA,
		ConvexShape shapeB, Vector2D posB,
		Vector2D direction) {

		// Get furthest point of A in direction
		Vector2D supportA = shapeA.getSupportPoint(direction);

		// Get furthest point of B in opposite direction
		Vector2D supportB = shapeB.getSupportPoint(direction.scale(-1));

		// Return difference (A - B) in world space
		return posA.add(supportA).subtract(posB.add(supportB));
	}

	/**
	 * Runs the Expanding Polytope Algorithm to calculate contact information.
	 *
	 * @param simplex The simplex from GJK that contains the origin
	 * @param shapeA First shape
	 * @param posA Position of first shape
	 * @param shapeB Second shape
	 * @param posB Position of second shape
	 * @return Contact information
	 */
	private ContactPoint runEPA(
		Simplex simplex,
		ConvexShape shapeA, Vector2D posA,
		ConvexShape shapeB, Vector2D posB) {

		// Convert simplex to list for EPA
		List<Vector2D> simplexPoints = new ArrayList<>();
		for (int i = 0; i < simplex.size(); i++) {
			simplexPoints.add(simplex.get(i));
		}

		// Run EPA to find penetration depth and contact point
		return EPA.findPenetration(simplexPoints, shapeA, posA, shapeB, posB);
	}
}