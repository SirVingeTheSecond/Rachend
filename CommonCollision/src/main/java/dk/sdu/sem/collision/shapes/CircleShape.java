package dk.sdu.sem.collision.shapes;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Circle-shaped collision shape.
 */
public class CircleShape implements ICollisionShape {
	private float radius;

	/**
	 * Empty Constructor
	 */
	public CircleShape() {

	}

	/**
	 * Creates a new circle shape.
	 *
	 * @param radius The radius of the circle
	 */
	public CircleShape(float radius) {
		this.radius = radius;
	}

	/**
	 * Gets the radius of the circle.
	 */
	public float getRadius() {
		return radius;
	}

	/**
	 * Sets the radius of the circle.
	 */
	public void setRadius(float radius) {
		this.radius = radius;
	}

	@Override
	public boolean intersects(ICollisionShape other) {
		if (other instanceof CircleShape circle) {
			return intersectsCircle(circle);
		} else if (other instanceof BoxShape box) {
			return intersectsBox(box);
		}
		return false;
	}

	/**
	 * Checks if this circle intersects with another circle.
	 */
	// ToDo:
	//  What should this be used for if it is always true
	//  Where should this be handled then?
	private boolean intersectsCircle(CircleShape other) {
		// For CircleShape, we need world positions but they're handled by the collision system
		// This method only checks if the circles would intersect if at the same position
		return true; // Always true, as position checks are handled elsewhere
	}

	/**
	 * Checks if this circle intersects with a box.
	 */
	// ToDo:
	//  What should this be used for if it is always true
	//  Where should this be handled then?
	private boolean intersectsBox(BoxShape box) {
		// For different shapes, we need to delegate to a shape-specific method
		// This will be handled by the collision detector
		return true; // Always true, as position checks are handled elsewhere
	}

	@Override
	public boolean contains(Vector2D point) {
		// The point is relative to the circle center, which is handled by the collision system
		return point.magnitudeSquared() <= radius * radius;
	}

	@Override
	public Bounds getBounds() {
		return new Bounds(-radius, -radius, radius * 2, radius * 2);
	}
}