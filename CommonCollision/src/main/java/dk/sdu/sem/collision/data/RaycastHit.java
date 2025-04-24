package dk.sdu.sem.collision.data;

import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Represents the result of a raycast operation.
 * Similar to Unity's RaycastHit.
 */
public class RaycastHit {
	private final boolean hit;
	private final Entity entity;
	private final Vector2D point;
	private final Vector2D normal;
	private final float distance;
	private final ColliderComponent collider;

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
	 * Checks if the ray hit something.
	 */
	public boolean isHit() {
		return hit;
	}

	/**
	 * Gets the entity that was hit.
	 * May be null for tilemaps or if nothing was hit.
	 */
	public Entity getEntity() {
		return entity;
	}

	/**
	 * Gets the world position of the hit.
	 * Null if nothing was hit.
	 */
	public Vector2D getPoint() {
		return point;
	}

	/**
	 * Gets the surface normal at the hit point.
	 * Null if nothing was hit.
	 */
	public Vector2D getNormal() {
		return normal;
	}

	/**
	 * Gets the distance from the ray origin to the hit point.
	 * Zero if nothing was hit.
	 */
	public float getDistance() {
		return distance;
	}

	/**
	 * Gets the collider that was hit.
	 * Null if nothing was hit.
	 */
	public ColliderComponent getCollider() {
		return collider;
	}
}