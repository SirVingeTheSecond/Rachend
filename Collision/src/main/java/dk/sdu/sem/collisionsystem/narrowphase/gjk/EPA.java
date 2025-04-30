package dk.sdu.sem.collisionsystem.narrowphase.gjk;

import dk.sdu.sem.collision.data.ContactPoint;
import dk.sdu.sem.collision.shapes.ConvexShape;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Expanding Polytope Algorithm (EPA) for finding the penetration depth and contact points
 * after GJK has determined that two convex shapes intersect.
 */
public class EPA {
	private static final int MAX_ITERATIONS = 20;
	private static final float TOLERANCE = 0.0001f;

	// Edge structure for EPA
	private static class Edge {
		int index;
		float distance;
		Vector2D normal;

		Edge(int index, float distance, Vector2D normal) {
			this.index = index;
			this.distance = distance;
			this.normal = normal;
		}
	}

	/**
	 * Finds the penetration information between two convex shapes.
	 *
	 * @param simplex The simplex from GJK that contains the origin
	 * @param shapeA The first shape
	 * @param posA The position of the first shape
	 * @param shapeB The second shape
	 * @param posB The position of the second shape
	 * @return Contact information or null if the algorithm fails
	 */
	public static ContactPoint findPenetration(
		List<Vector2D> simplex,
		ConvexShape shapeA, Vector2D posA,
		ConvexShape shapeB, Vector2D posB) {

		// Convert simplex to a polytope
		List<Vector2D> polytope = new ArrayList<>(simplex);

		// Iterate to expand the polytope
		for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
			// Find the closest edge to the origin
			Edge closestEdge = findClosestEdge(polytope);

			// Get support point in the direction of the normal
			Vector2D supportPoint = getMinkowskiDifference(
				shapeA, posA, shapeB, posB, closestEdge.normal);

			// Calculate distance from support point to edge
			float distance = supportPoint.dot(closestEdge.normal);

			// Check if we've reached the edge of the Minkowski Difference
			if (Math.abs(distance - closestEdge.distance) < TOLERANCE) {
				// We've found the edge, create contact point
				return createContactPoint(closestEdge.normal, distance, shapeA, posA, shapeB, posB);
			}

			// Otherwise, add the new point to the polytope
			polytope.add(closestEdge.index + 1, supportPoint);
		}

		// If we reach this point, EPA failed to converge
		// Return contact based on last known closest edge
		Edge bestGuess = findClosestEdge(polytope);
		return createContactPoint(bestGuess.normal, bestGuess.distance, shapeA, posA, shapeB, posB);
	}

	/**
	 * Finds the edge of the polytope closest to the origin.
	 *
	 * @param polytope The polytope vertices
	 * @return The closest edge information
	 */
	private static Edge findClosestEdge(List<Vector2D> polytope) {
		Edge closestEdge = null;
		float minDistance = Float.MAX_VALUE;

		for (int i = 0; i < polytope.size(); i++) {
			int j = (i + 1) % polytope.size();

			Vector2D a = polytope.get(i);
			Vector2D b = polytope.get(j);

			// Get the edge vector
			Vector2D edge = b.subtract(a);

			// Get the normal (perpendicular to edge, pointing inward)
			Vector2D normal = new Vector2D(edge.y(), -edge.x()).normalize();

			// Make sure normal points toward origin
			if (normal.dot(a) < 0) {
				normal = normal.scale(-1);
			}

			// Calculate distance from origin to edge
			float distance = a.dot(normal);

			// Keep track of minimum
			if (distance < minDistance) {
				minDistance = distance;
				closestEdge = new Edge(i, distance, normal);
			}
		}

		return closestEdge;
	}

	/**
	 * Creates a contact point from the EPA result.
	 *
	 * @param normal The collision normal
	 * @param depth The penetration depth
	 * @param shapeA The first shape
	 * @param posA The position of the first shape
	 * @param shapeB The second shape
	 * @param posB The position of the second shape
	 * @return The contact point information
	 */
	private static ContactPoint createContactPoint(
		Vector2D normal, float depth,
		ConvexShape shapeA, Vector2D posA,
		ConvexShape shapeB, Vector2D posB) {

		// Invert the normal as it points from B to A in our convention
		normal = normal.scale(-1);

		// Find the support points on each shape in the direction of the normal
		Vector2D supportA = posA.add(shapeA.getSupportPoint(normal));
		Vector2D supportB = posB.add(shapeB.getSupportPoint(normal.scale(-1)));

		// Contact point is typically in the middle of the overlap region
		Vector2D contactPoint = supportA.add(supportB).scale(0.5f);

		// Create the contact point with penetration depth
		return new ContactPoint(contactPoint, normal, depth);
	}

	/**
	 * Gets the Minkowski Difference support point for two shapes.
	 *
	 * @param shapeA The first shape
	 * @param posA The position of the first shape
	 * @param shapeB The second shape
	 * @param posB The position of the second shape
	 * @param direction The direction to get the support point in
	 * @return The support point in the Minkowski Difference
	 */
	private static Vector2D getMinkowskiDifference(
		ConvexShape shapeA, Vector2D posA,
		ConvexShape shapeB, Vector2D posB,
		Vector2D direction) {

		// Get furthest point of A in direction
		Vector2D supportA = posA.add(shapeA.getSupportPoint(direction));

		// Get furthest point of B in opposite direction
		Vector2D supportB = posB.add(shapeB.getSupportPoint(direction.scale(-1)));

		// Return difference (A - B)
		return supportA.subtract(supportB);
	}
}