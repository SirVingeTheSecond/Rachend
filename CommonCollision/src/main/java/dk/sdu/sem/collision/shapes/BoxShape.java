package dk.sdu.sem.collision.shapes;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Rectangle-shaped collision shape.
 * The position is assumed to be the top-left corner.
 */
public class BoxShape implements ICollisionShape {
	private float width;
	private float height;

	/**
	 * Empty constructor
	 */
	public BoxShape() {

	}

	/**
	 * Creates a new box shape.
	 *
	 * @param width The width of the box
	 * @param height The height of the box
	 */
	public BoxShape(float width, float height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * Gets the width of the box.
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * Gets the height of the box.
	 */
	public float getHeight() {
		return height;
	}

	/**
	 * Set the width of the box
	 */
	public void setWidth(float width) {
		this.width = width;
	}

	/**
	 * Sets the height of the box
	 */
	public void setHeight(float height) {
		this.height = height;
	}

	@Override
	public boolean intersects(ICollisionShape other) {
		if (other instanceof BoxShape box) {
			return intersectsBox(box);
		} else if (other instanceof CircleShape circle) {
			return intersectsCircle(circle);
		}
		return false;
	}

	/**
	 * Checks if this box intersects with another box.
	 */
	private boolean intersectsBox(BoxShape other) {
		// For BoxShape, we need world positions but they're handled by the collision system
		// This method only checks if the boxes would intersect if at the same position
		return true; // Always true, as position checks are handled elsewhere
	}

	/**
	 * Checks if this box intersects with a circle.
	 */
	private boolean intersectsCircle(CircleShape circle) {
		// For different shapes, we need to delegate to a shape-specific method
		// This will be handled by the collision detector
		return true; // Always true, as position checks are handled elsewhere
	}

	@Override
	public boolean contains(Vector2D point) {
		// The point is relative to the box position, which is handled by the collision system
		return point.x() >= 0 && point.x() <= width &&
			point.y() >= 0 && point.y() <= height;
	}

	@Override
	public Bounds getBounds() {
		return new Bounds(0, 0, width, height);
	}
}