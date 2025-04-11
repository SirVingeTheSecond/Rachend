package dk.sdu.sem.collisionsystem.narrowphase.solvers;

import dk.sdu.sem.collision.ContactPoint;
import dk.sdu.sem.collision.shapes.BoxShape;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Handles circle vs box collisions
 */
public class CircleBoxSolver implements IShapeSolver<CircleShape, BoxShape> {
	private static final float EPSILON = 0.0001f;

	@Override
	public ContactPoint solve(CircleShape circle, Vector2D circlePos, BoxShape box, Vector2D boxPos) {
		float radius = circle.getRadius();
		float boxWidth = box.getWidth();
		float boxHeight = box.getHeight();

		// Box boundaries
		float boxLeft = boxPos.x();
		float boxRight = boxPos.x() + boxWidth;
		float boxTop = boxPos.y();
		float boxBottom = boxPos.y() + boxHeight;

		// Find closest point on box to circle center
		float closestX = Math.max(boxLeft, Math.min(circlePos.x(), boxRight));
		float closestY = Math.max(boxTop, Math.min(circlePos.y(), boxBottom));

		// Calculate vector from closest point to circle center
		Vector2D closestPoint = new Vector2D(closestX, closestY);
		Vector2D toCircle = circlePos.subtract(closestPoint);
		float distanceSquared = toCircle.magnitudeSquared();

		// Check for collision
		if (distanceSquared < radius * radius) {
			float distance = (float) Math.sqrt(distanceSquared);

			// Calculate normal and penetration
			Vector2D normal;
			float penetration;

			if (distance < EPSILON) {
				// Circle center is inside box, find closest edge
				float leftDist = circlePos.x() - boxLeft;
				float rightDist = boxRight - circlePos.x();
				float topDist = circlePos.y() - boxTop;
				float bottomDist = boxBottom - circlePos.y();

				// Find smallest distance to edge
				float minDist = Math.min(Math.min(leftDist, rightDist), Math.min(topDist, bottomDist));

				if (minDist == leftDist) normal = new Vector2D(-1, 0);
				else if (minDist == rightDist) normal = new Vector2D(1, 0);
				else if (minDist == topDist) normal = new Vector2D(0, -1);
				else normal = new Vector2D(0, 1);

				penetration = radius + minDist;
			} else {
				// Normal points from closest point to circle center
				normal = distance > EPSILON ?
					toCircle.scale(1f / distance) : new Vector2D(1, 0);
				penetration = radius - distance;
			}

			// Calculate contact point
			Vector2D contactPoint = circlePos.subtract(normal.scale(radius));

			return new ContactPoint(contactPoint, normal, penetration);
		}

		return null;
	}
}
