package dk.sdu.sem.collision.shapes;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Rectangle-shaped collision shape.
 * The position is assumed to be the top-left corner.
 */
public class BoxShape implements ConvexShape {
	private float width;
	private float height;

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
		// Default implementation, actual collision detection is handled by the collision system
		return false;
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

	@Override
	public Vector2D getSupportPoint(Vector2D direction) {
		// For an AABB, the support point is the vertex that maximizes the dot product
		// with the direction vector

		// Choose the right vertex based on direction components
		float x = (direction.x() >= 0) ? width : 0;
		float y = (direction.y() >= 0) ? height : 0;

		return new Vector2D(x, y);
	}

	@Override
	public Vector2D getCenter() {
		return new Vector2D(width / 2, height / 2);
	}
}