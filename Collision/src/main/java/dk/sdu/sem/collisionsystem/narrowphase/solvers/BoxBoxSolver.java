package dk.sdu.sem.collisionsystem.narrowphase.solvers;

import dk.sdu.sem.collision.data.ContactPoint;
import dk.sdu.sem.collision.shapes.BoxShape;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Handles box vs box collisions
 */
public class BoxBoxSolver implements IShapeSolver<BoxShape, BoxShape> {
	private static final float EPSILON = 0.0001f;

	@Override
	public ContactPoint solve(BoxShape boxA, Vector2D posA, BoxShape boxB, Vector2D posB) {
		float widthA = boxA.getWidth();
		float heightA = boxA.getHeight();
		float widthB = boxB.getWidth();
		float heightB = boxB.getHeight();

		// Calculate bounds
		float leftA = posA.x();
		float rightA = posA.x() + widthA;
		float topA = posA.y();
		float bottomA = posA.y() + heightA;

		float leftB = posB.x();
		float rightB = posB.x() + widthB;
		float topB = posB.y();
		float bottomB = posB.y() + heightB;

		// Check for intersection (AABB test)
		if (leftA < rightB && rightA > leftB && topA < bottomB && bottomA > topB) {
			// Calculate overlap on each axis
			float overlapX = Math.min(rightA, rightB) - Math.max(leftA, leftB);
			float overlapY = Math.min(bottomA, bottomB) - Math.max(topA, topB);

			// Use smaller overlap to determine collision normal
			Vector2D normal;
			float penetration;

			if (overlapX < overlapY) {
				// X axis has smaller overlap - determine direction
				normal = (posA.x() < posB.x()) ? new Vector2D(1, 0) : new Vector2D(-1, 0);
				penetration = overlapX;
			} else {
				// Y axis has smaller overlap - determine direction
				normal = (posA.y() < posB.y()) ? new Vector2D(0, 1) : new Vector2D(0, -1);
				penetration = overlapY;
			}

			// Calculate contact point at center of overlap region
			float contactX = Math.max(leftA, leftB) + (overlapX / 2);
			float contactY = Math.max(topA, topB) + (overlapY / 2);

			return new ContactPoint(new Vector2D(contactX, contactY), normal, penetration);
		}

		return null;
	}
}