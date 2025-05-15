package dk.sdu.sem.collision.data;

import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Represents the result of a raycast.
 */
public class RaycastHit {
	private boolean hit;
	private Entity entity;
	private Vector2D point;
	private Vector2D normal;
	private float distance;
	private ColliderComponent collider;

	/**
	 * Creates a no-hit result.
	 */
	public static RaycastHit noHit() {
		return new RaycastHit(false, null, null, null, 0, null);
	}

	/**
	 * Creates a new raycast hit result.
	 *
	 * @param hit Whether the ray hit something
	 * @param entity The entity that was hit (may be null for tilemaps)
	 * @param point The world position of the hit
	 * @param normal The surface normal at the hit point
	 * @param distance The distance from the ray origin to the hit point
	 * @param collider The collider that was hit
	 */
	public RaycastHit(boolean hit, Entity entity, Vector2D point, Vector2D normal, float distance, ColliderComponent collider) {
		this.hit = hit;
		this.entity = entity;
		this.point = point;
		this.normal = normal;
		this.distance = distance;
		this.collider = collider;
	}

	/**
	 * Default constructor for non-allocating APIs.
	 */
	public RaycastHit() {
		this(false, null, null, null, 0, null);
	}

	/**
	 * Checks if the ray hit something.
	 */
	public boolean isHit() {
		return hit;
	}

	/**
	 * Sets whether the ray hit something.
	 * Used for non-allocating APIs.
	 *
	 * @param hit Whether the ray hit something
	 */
	public void setHit(boolean hit) {
		this.hit = hit;
	}

	/**
	 * Gets the entity that was hit.
	 * May be null for tilemaps or if nothing was hit.
	 */
	public Entity getEntity() {
		return entity;
	}

	/**
	 * Sets the entity that was hit.
	 * Used for non-allocating APIs.
	 *
	 * @param entity The entity that was hit
	 */
	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	/**
	 * Gets the world position of the hit.
	 * Null if nothing was hit.
	 */
	public Vector2D getPoint() {
		return point;
	}

	/**
	 * Sets the world position of the hit.
	 * Used for non-allocating APIs.
	 *
	 * @param point The world position of the hit
	 */
	public void setPoint(Vector2D point) {
		this.point = point;
	}

	/**
	 * Gets the surface normal at the hit point.
	 * Null if nothing was hit.
	 */
	public Vector2D getNormal() {
		return normal;
	}

	/**
	 * Sets the surface normal at the hit point.
	 * Used for non-allocating APIs.
	 *
	 * @param normal The surface normal
	 */
	public void setNormal(Vector2D normal) {
		this.normal = normal;
	}

	/**
	 * Gets the distance from the ray origin to the hit point.
	 * Zero if nothing was hit.
	 */
	public float getDistance() {
		return distance;
	}

	/**
	 * Sets the distance from the ray origin to the hit point.
	 * Used for non-allocating APIs.
	 *
	 * @param distance The distance
	 */
	public void setDistance(float distance) {
		this.distance = distance;
	}

	/**
	 * Gets the collider that was hit.
	 * Null if nothing was hit.
	 */
	public ColliderComponent getCollider() {
		return collider;
	}

	/**
	 * Sets the collider that was hit.
	 * Used for non-allocating APIs.
	 *
	 * @param collider The collider
	 */
	public void setCollider(ColliderComponent collider) {
		this.collider = collider;
	}

	/**
	 * Copies values from another RaycastHit.
	 * Used for non-allocating APIs.
	 *
	 * @param other The hit to copy from
	 */
	public void copyFrom(RaycastHit other) {
		this.hit = other.hit;
		this.entity = other.entity;
		this.point = other.point;
		this.normal = other.normal;
		this.distance = other.distance;
		this.collider = other.collider;
	}

	@Override
	public String toString() {
		if (!hit) {
			return "RaycastHit: No hit";
		}
		return String.format("RaycastHit: [Entity: %s, Distance: %.2f, Point: %s, Normal: %s]",
			entity != null ? entity.getID() : "null",
			distance,
			point != null ? point.toString() : "null",
			normal != null ? normal.toString() : "null");
	}
}