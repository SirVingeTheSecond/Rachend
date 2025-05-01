package dk.sdu.sem.collisionsystem.narrowphase.solvers;

import dk.sdu.sem.collision.data.ContactPoint;
import dk.sdu.sem.collision.shapes.BoxShape;
import dk.sdu.sem.collision.shapes.CircleShape;
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
		// Special case for circles - exact collision check
		if (shapeA instanceof CircleShape && shapeB instanceof CircleShape) {
			return solveCircleCircle((CircleShape)shapeA, posA, (CircleShape)shapeB, posB);
		}

		// Check for containment cases
		ContactPoint containmentContact = checkContainment(shapeA, posA, shapeB, posB);
		if (containmentContact != null) {
			return containmentContact;
		}

		// For other convex shapes, use GJK algorithm
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
	 * Checks for cases where one shape might be contained within another.
	 * GJK can have difficulty with these cases, so we handle them specially.
	 *
	 * @param shapeA First shape
	 * @param posA Position of first shape
	 * @param shapeB Second shape
	 * @param posB Position of second shape
	 * @return ContactPoint if containment detected, null otherwise
	 */
	private ContactPoint checkContainment(ConvexShape shapeA, Vector2D posA, ConvexShape shapeB, Vector2D posB) {
		// Check for box and circle containment cases
		if (shapeA instanceof BoxShape && shapeB instanceof CircleShape) {
			return checkBoxContainingCircle((BoxShape)shapeA, posA, (CircleShape)shapeB, posB);
		}
		else if (shapeA instanceof CircleShape && shapeB instanceof BoxShape) {
			return checkCircleContainingBox((CircleShape)shapeA, posA, (BoxShape)shapeB, posB);
		}
		else if (shapeA instanceof BoxShape && shapeB instanceof BoxShape) {
			return checkBoxContainingBox((BoxShape)shapeA, posA, (BoxShape)shapeB, posB);
		}

		return null;
	}

	/**
	 * Checks if a box contains a circle.
	 */
	private ContactPoint checkBoxContainingCircle(BoxShape box, Vector2D boxPos, CircleShape circle, Vector2D circlePos) {
		float boxWidth = box.getWidth();
		float boxHeight = box.getHeight();
		float circleRadius = circle.getRadius();

		// Get centers
		Vector2D boxCenter = boxPos.add(box.getCenter());
		Vector2D circleCenter = circlePos.add(circle.getCenter());

		// Vector from box center to circle center
		Vector2D centerDiff = circleCenter.subtract(boxCenter);

		// Calculate AABB of the box
		float boxMinX = boxPos.x();
		float boxMinY = boxPos.y();
		float boxMaxX = boxMinX + boxWidth;
		float boxMaxY = boxMinY + boxHeight;

		// Check if circle is inside box
		if (circleCenter.x() > boxMinX + circleRadius &&
			circleCenter.x() < boxMaxX - circleRadius &&
			circleCenter.y() > boxMinY + circleRadius &&
			circleCenter.y() < boxMaxY - circleRadius) {

			// Circle is fully inside box - find closest edge
			float distToLeft = circleCenter.x() - boxMinX;
			float distToRight = boxMaxX - circleCenter.x();
			float distToTop = circleCenter.y() - boxMinY;
			float distToBottom = boxMaxY - circleCenter.y();

			float minDist = Math.min(Math.min(distToLeft, distToRight), Math.min(distToTop, distToBottom));
			Vector2D normal;

			// Create normal pointing toward the closest edge
			if (minDist == distToLeft) {
				normal = new Vector2D(-1, 0);
				minDist = distToLeft - circleRadius;
			} else if (minDist == distToRight) {
				normal = new Vector2D(1, 0);
				minDist = distToRight - circleRadius;
			} else if (minDist == distToTop) {
				normal = new Vector2D(0, -1);
				minDist = distToTop - circleRadius;
			} else {
				normal = new Vector2D(0, 1);
				minDist = distToBottom - circleRadius;
			}

			// Penetration depth is distance to closest edge minus radius
			return new ContactPoint(circleCenter, normal, minDist);
		}

		return null;
	}

	/**
	 * Checks if a circle contains a box.
	 */
	private ContactPoint checkCircleContainingBox(CircleShape circle, Vector2D circlePos, BoxShape box, Vector2D boxPos) {
		float circleRadius = circle.getRadius();
		float boxWidth = box.getWidth();
		float boxHeight = box.getHeight();

		// Get centers
		Vector2D circleCenter = circlePos.add(circle.getCenter());
		Vector2D boxCenter = boxPos.add(box.getCenter());

		// Vector from circle center to box center
		Vector2D centerDiff = boxCenter.subtract(circleCenter);
		float centerDistance = centerDiff.magnitude();

		// Calculate the furthest point of the box from its center
		float boxCornerDist = (float) Math.sqrt((boxWidth * boxWidth + boxHeight * boxHeight) / 4);

		// If furthest point of box is inside circle
		if (centerDistance + boxCornerDist <= circleRadius) {
			// Box is fully inside circle
			Vector2D normal = centerDistance > EPSILON ?
				centerDiff.scale(1/centerDistance) : new Vector2D(1, 0);

			// Penetration is radius minus (distance to center + distance to furthest corner)
			float penetration = circleRadius - (centerDistance + boxCornerDist);
			return new ContactPoint(boxCenter, normal, penetration);
		}

		return null;
	}

	/**
	 * Checks if one box contains another box.
	 */
	private ContactPoint checkBoxContainingBox(BoxShape boxA, Vector2D posA, BoxShape boxB, Vector2D posB) {
		float widthA = boxA.getWidth();
		float heightA = boxA.getHeight();
		float widthB = boxB.getWidth();
		float heightB = boxB.getHeight();

		// Calculate AABBs
		float minAx = posA.x();
		float minAy = posA.y();
		float maxAx = minAx + widthA;
		float maxAy = minAy + heightA;

		float minBx = posB.x();
		float minBy = posB.y();
		float maxBx = minBx + widthB;
		float maxBy = minBy + heightB;

		// Check if B is contained in A
		if (minBx >= minAx && maxBx <= maxAx && minBy >= minAy && maxBy <= maxAy) {
			// B is inside A - find closest edge
			float distToLeft = minBx - minAx;
			float distToRight = maxAx - maxBx;
			float distToTop = minBy - minAy;
			float distToBottom = maxAy - maxBy;

			float minDist = Math.min(Math.min(distToLeft, distToRight), Math.min(distToTop, distToBottom));
			Vector2D normal;
			Vector2D centerB = posB.add(boxB.getCenter());

			// Create normal pointing toward the closest edge
			if (minDist == distToLeft) {
				normal = new Vector2D(-1, 0);
			} else if (minDist == distToRight) {
				normal = new Vector2D(1, 0);
			} else if (minDist == distToTop) {
				normal = new Vector2D(0, -1);
			} else {
				normal = new Vector2D(0, 1);
			}

			return new ContactPoint(centerB, normal, minDist);
		}

		// Check if A is contained in B
		if (minAx >= minBx && maxAx <= maxBx && minAy >= minBy && maxAy <= maxBy) {
			// A is inside B - find closest edge
			float distToLeft = minAx - minBx;
			float distToRight = maxBx - maxAx;
			float distToTop = minAy - minBy;
			float distToBottom = maxBy - maxAy;

			float minDist = Math.min(Math.min(distToLeft, distToRight), Math.min(distToTop, distToBottom));
			Vector2D normal;
			Vector2D centerA = posA.add(boxA.getCenter());

			// Create normal pointing toward B from A
			if (minDist == distToLeft) {
				normal = new Vector2D(1, 0);
			} else if (minDist == distToRight) {
				normal = new Vector2D(-1, 0);
			} else if (minDist == distToTop) {
				normal = new Vector2D(0, 1);
			} else {
				normal = new Vector2D(0, -1);
			}

			return new ContactPoint(centerA, normal, minDist);
		}

		return null;
	}

	/**
	 * Specialized solver for circle-circle collisions.
	 * This provides exact results for circle collisions.
	 *
	 * @param circleA First circle
	 * @param posA Position of first circle
	 * @param circleB Second circle
	 * @param posB Position of second circle
	 * @return Contact information or null if no collision
	 */
	private ContactPoint solveCircleCircle(CircleShape circleA, Vector2D posA, CircleShape circleB, Vector2D posB) {
		float radiusA = circleA.getRadius();
		float radiusB = circleB.getRadius();
		float radiusSum = radiusA + radiusB;

		// Calculate vector between centers
		Vector2D centerA = posA.add(circleA.getCenter());
		Vector2D centerB = posB.add(circleB.getCenter());
		Vector2D direction = centerB.subtract(centerA);
		float distanceSquared = direction.magnitudeSquared();
		float distance = (float) Math.sqrt(distanceSquared);

		// Special case: check for one circle inside the other
		if (distance < Math.abs(radiusA - radiusB)) {
			// One circle is inside the other

			Vector2D normal;
			float penetration;

			if (radiusA > radiusB) {
				// B is inside A
				// Normal should point from A to B to push B outward
				normal = distance > EPSILON ? direction.normalize() : new Vector2D(1, 0);
				penetration = radiusA - radiusB - distance;
			} else {
				// A is inside B
				// Normal should point from B to A to push A outward
				normal = distance > EPSILON ? direction.normalize().scale(-1) : new Vector2D(-1, 0);
				penetration = radiusB - radiusA - distance;
			}

			// Calculate contact point in the middle
			Vector2D contactPoint = centerA.add(centerB).scale(0.5f);

			return new ContactPoint(contactPoint, normal, penetration);
		}
		// Special case: partially contained circles
		else if (distanceSquared < radiusSum * radiusSum) {
			// Normal should point from A to B according to the physics engine's expectation
			Vector2D normal;
			if (distance > EPSILON) {
				normal = direction.normalize(); // Point from A to B
			} else {
				// Centers are at same position, use default normal
				normal = new Vector2D(1, 0);
			}

			// Calculate penetration depth
			float penetration = radiusSum - distance;

			// Calculate contact point (between the circles)
			Vector2D contactPoint = centerA.add(normal.scale(radiusA));

			return new ContactPoint(contactPoint, normal, penetration);
		}

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

		// Ensure direction is not a zero vector
		if (direction.magnitudeSquared() < EPSILON) {
			direction = new Vector2D(1, 0);
		}

		// Get furthest point of A in direction
		Vector2D supportA = posA.add(shapeA.getSupportPoint(direction));

		// Get furthest point of B in opposite direction
		Vector2D supportB = posB.add(shapeB.getSupportPoint(direction.scale(-1)));

		// Return difference (A - B) in world space
		return supportA.subtract(supportB);
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