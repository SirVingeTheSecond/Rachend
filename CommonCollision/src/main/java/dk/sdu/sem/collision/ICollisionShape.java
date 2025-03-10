package dk.sdu.sem.collision;

public interface ICollisionShape {
	/**
	 * Checks if this shape intersects with another shape.
	 *
	 * @param other The other collision shape.
	 * @return true if the shapes intersect, false otherwise.
	 */
	boolean intersects(ICollisionShape other);
}
