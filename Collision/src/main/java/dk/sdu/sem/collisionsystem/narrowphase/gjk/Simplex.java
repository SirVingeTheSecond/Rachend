package dk.sdu.sem.collisionsystem.narrowphase.gjk;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Represents a simplex for the GJK algorithm.
 * A simplex can have up to 3 points.
 */
public class Simplex {
	private final Vector2D[] points;
	private int size;

	/**
	 * Creates a new, empty simplex.
	 */
	public Simplex() {
		points = new Vector2D[3]; // Maximum 3 points for 2D simplex
		size = 0;
	}

	/**
	 * Adds a point to the simplex.
	 *
	 * @param point The point to add
	 */
	public void add(Vector2D point) {
		if (size >= 3) {
			// Shift points to make room
			points[0] = points[1];
			points[1] = points[2];
			points[2] = point;
		} else {
			points[size] = point;
			size++;
		}
	}

	/**
	 * Gets a point from the simplex by index.
	 *
	 * @param index The index of the point to get
	 * @return The point at the specified index
	 */
	public Vector2D get(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Simplex index out of bounds: " + index);
		}
		return points[index];
	}

	/**
	 * Gets the number of points in the simplex.
	 *
	 * @return The size of the simplex
	 */
	public int size() {
		return size;
	}

	/**
	 * Clears the simplex.
	 */
	public void clear() {
		size = 0;
	}

	// This method is not needed and has been removed

	/**
	 * Gets the last point added to the simplex.
	 *
	 * @return The last point added
	 */
	public Vector2D getLast() {
		if (size == 0) {
			throw new IllegalStateException("Cannot get last point from empty simplex");
		}
		return points[size - 1];
	}

	/**
	 * Calculates whether the simplex contains the origin.
	 * Also updates the simplex and the search direction.
	 *
	 * @param direction The current search direction, will be updated
	 * @return True if the simplex contains the origin, false otherwise
	 */
	public boolean containsOrigin(Vector2D[] direction) {
		switch (size) {
			case 1:
				// Line case - can't contain origin, set direction towards origin
				direction[0] = points[0].scale(-1);
				return false;

			case 2:
				// Line segment case
				return handleLineCase(direction);

			case 3:
				// Triangle case
				return handleTriangleCase(direction);

			default:
				return false;
		}
	}

	/**
	 * Handles the line segment case for simplex.
	 *
	 * @param direction The direction to update
	 * @return True if contains origin, false otherwise
	 */
	private boolean handleLineCase(Vector2D[] direction) {
		Vector2D a = points[1]; // Last point added
		Vector2D b = points[0]; // Previous point

		Vector2D ab = b.subtract(a);
		Vector2D ao = new Vector2D(0, 0).subtract(a); // Vector from A to origin

		if (sameDirection(ab, ao)) {
			// Origin is on the side of AB
			// Set direction perpendicular to AB towards origin
			direction[0] = ab.perpendicular();

			// Ensure perpendicular direction points towards origin
			if (direction[0].dot(ao) < 0) {
				direction[0] = direction[0].scale(-1);
			}
		} else {
			// Origin is on the side of A
			// Simplex becomes just point A
			direction[0] = ao;
			points[0] = a;
			size = 1;
		}

		return false; // Line segment can't contain origin
	}

	/**
	 * Handles the triangle case for simplex.
	 *
	 * @param direction The direction to update
	 * @return True if contains origin, false otherwise
	 */
	private boolean handleTriangleCase(Vector2D[] direction) {
		Vector2D a = points[2]; // Last point added
		Vector2D b = points[1]; // Second-to-last point
		Vector2D c = points[0]; // First point

		Vector2D ab = b.subtract(a);
		Vector2D ac = c.subtract(a);
		Vector2D ao = new Vector2D(0, 0).subtract(a); // Vector from A to origin

		Vector2D abPerp = tripleProduct(ac, ab, ab);
		Vector2D acPerp = tripleProduct(ab, ac, ac);

		if (sameDirection(abPerp, ao)) {
			// Origin outside AB edge
			// Remove point C, simplex becomes line AB
			points[0] = b;
			points[1] = a;
			size = 2;
			direction[0] = abPerp;
			return false;
		}

		if (sameDirection(acPerp, ao)) {
			// Origin outside AC edge
			// Remove point B, simplex becomes line AC
			points[0] = c;
			points[1] = a;
			size = 2;
			direction[0] = acPerp;
			return false;
		}

		// Origin is inside both AB and AC regions
		// The origin is inside the triangle
		return true;
	}

	/**
	 * Checks if two vectors point in the same general direction.
	 *
	 * @param a First vector
	 * @param b Second vector
	 * @return True if a and b point in the same direction (dot product is positive)
	 */
	private boolean sameDirection(Vector2D a, Vector2D b) {
		return a.dot(b) > 0;
	}

	/**
	 * Calculates the triple product (a × b) × c.
	 * Used to find perpendicular vectors in GJK.
	 *
	 * @param a First vector
	 * @param b Second vector
	 * @param c Third vector
	 * @return The triple product (a × b) × c
	 */
	private Vector2D tripleProduct(Vector2D a, Vector2D b, Vector2D c) {
		// In 2D, we can simplify the triple product calculation
		// Instead of doing full cross products, we can:
		// 1. Get perpendicular to a × b (which is a scalar in 2D)
		// 2. Then find a perpendicular to c that points in the right direction

		// In 2D cross product: a × b = (a.x * b.y - a.y * b.x)
		float crossProduct = a.x() * b.y() - a.y() * b.x();

		// Create perpendicular to c
		Vector2D perpC = new Vector2D(-c.y(), c.x());

		// Ensure it points in the right direction (sign of cross product)
		if (crossProduct < 0) {
			perpC = perpC.scale(-1);
		}

		return perpC;
	}
}