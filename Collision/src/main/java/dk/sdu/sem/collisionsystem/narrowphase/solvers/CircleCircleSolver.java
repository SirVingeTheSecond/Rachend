package dk.sdu.sem.collisionsystem.narrowphase.solvers;

import dk.sdu.sem.collision.ContactPoint;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Handles circle vs circle collisions
 */
public class CircleCircleSolver implements IShapeSolver<CircleShape, CircleShape> {
	private static final float EPSILON = 0.0001f;

	@Override
	public ContactPoint solve(CircleShape circleA, Vector2D posA, CircleShape circleB, Vector2D posB) {
		float radiusA = circleA.getRadius();
		float radiusB = circleB.getRadius();
		float radiusSum = radiusA + radiusB;

		// Calculate vector between centers
		Vector2D direction = posB.subtract(posA);
		float distanceSquared = direction.magnitudeSquared();

		// Check for collision
		if (distanceSquared < radiusSum * radiusSum) {
			float distance = (float) Math.sqrt(distanceSquared);

			// Normalize direction
			Vector2D normal = distance > EPSILON ?
				direction.scale(1f / distance) : new Vector2D(1, 0);

			// Calculate penetration depth
			float penetration = radiusSum - distance;

			// Calculate contact point (on surface of circle A)
			Vector2D contactPoint = posA.add(normal.scale(radiusA));

			return new ContactPoint(contactPoint, normal, penetration);
		}

		return null;
	}
}