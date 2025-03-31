package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Result of a raycast operation.
 */
public class RaycastResult {
	private final boolean hit;
	private final Entity hitEntity;
	private final Vector2D hitPoint;
	private final Vector2D hitNormal;
	private final float distance;

	// No-hit constructor
	public static RaycastResult noHit() {
		return new RaycastResult(false, null, null, null, Float.MAX_VALUE);
	}

	public RaycastResult(boolean hit, Entity hitEntity, Vector2D hitPoint, Vector2D hitNormal, float distance) {
		this.hit = hit;
		this.hitEntity = hitEntity;
		this.hitPoint = hitPoint;
		this.hitNormal = hitNormal;
		this.distance = distance;
	}

	public boolean isHit() { return hit; }
	public Entity getHitEntity() { return hitEntity; }
	public Vector2D getHitPoint() { return hitPoint; }
	public Vector2D getHitNormal() { return hitNormal; }
	public float getDistance() { return distance; }
}