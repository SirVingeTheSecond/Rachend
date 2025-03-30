package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Contains detailed information about a detected collision.
 * Result of the narrow phase collision detection.
 */
public class CollisionData {
	private final Entity entityA;
	private final Entity entityB;
	private final boolean isTriggerA;
	private final boolean isTriggerB;
	private final Vector2D collisionPoint;
	private final Vector2D normal;
	private final float penetrationDepth;

	public CollisionData(Entity entityA, Entity entityB, boolean isTriggerA, boolean isTriggerB) {
		this(entityA, entityB, isTriggerA, isTriggerB, null, null, 0.0f);
	}

	public CollisionData(
		Entity entityA,
		Entity entityB,
		boolean isTriggerA,
		boolean isTriggerB,
		Vector2D collisionPoint,
		Vector2D normal,
		float penetrationDepth) {
		this.entityA = entityA;
		this.entityB = entityB;
		this.isTriggerA = isTriggerA;
		this.isTriggerB = isTriggerB;
		this.collisionPoint = collisionPoint;
		this.normal = normal;
		this.penetrationDepth = penetrationDepth;
	}

	public Entity getEntityA() {
		return entityA;
	}

	public Entity getEntityB() {
		return entityB;
	}

	public boolean isTriggerA() {
		return isTriggerA;
	}

	public boolean isTriggerB() {
		return isTriggerB;
	}

	public boolean isTriggerCollision() {
		return isTriggerA || isTriggerB;
	}

	public Vector2D getCollisionPoint() {
		return collisionPoint;
	}

	public Vector2D getNormal() {
		return normal;
	}

	public float getPenetrationDepth() {
		return penetrationDepth;
	}

	/**
	 * Creates a canonical representation where entityA's ID is less than entityB's.
	 * This ensures consistent tracking regardless of the order entities are passed in.
	 */
	public CollisionData getCanonical() {
		if (entityA.getID().compareTo(entityB.getID()) <= 0) {
			return this;
		} else {
			return new CollisionData(
				entityB, entityA, isTriggerB, isTriggerA,
				collisionPoint,
				normal != null ? normal.scale(-1) : null,
				penetrationDepth
			);
		}
	}
}