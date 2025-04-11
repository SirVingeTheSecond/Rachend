package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.CollisionPair;
import dk.sdu.sem.collision.ContactPoint;
import dk.sdu.sem.commonsystem.Entity;

/**
 * Represents a collision between a regular collider and a tilemap tile.
 */
public class TilemapCollisionPair extends CollisionPair {
	private final int tileX;
	private final int tileY;
	private final Entity tilemapEntity;

	/**
	 * Creates a new collision pair between a collider and a tilemap tile.
	 *
	 * @param colliderNode The node for the colliding entity
	 * @param tilemapNode The node for the tilemap entity
	 * @param tileX X coordinate of the tile that was hit
	 * @param tileY Y coordinate of the tile that was hit
	 * @param contact Contact point information
	 * @param isTrigger Whether this is a trigger collision
	 */
	public TilemapCollisionPair(
		ColliderNode colliderNode,
		TilemapColliderNode tilemapNode,
		int tileX,
		int tileY,
		ContactPoint contact,
		boolean isTrigger) {

		super(colliderNode.getEntity(),
			tilemapNode.getEntity(),
			colliderNode.collider,
			tilemapNode.tilemapCollider,
			contact,
			isTrigger
		);

		this.tileX = tileX;
		this.tileY = tileY;
		this.tilemapEntity = tilemapNode.getEntity();
	}

	/**
	 * Gets the X coordinate of the tile that was hit.
	 */
	public int getTileX() {
		return tileX;
	}

	/**
	 * Gets the Y coordinate of the tile that was hit.
	 */
	public int getTileY() {
		return tileY;
	}

	/**
	 * Gets the ID for this collision pair.
	 * Overridden to include the tile coordinates, making each tile collision unique.
	 */
	@Override
	public String getId() {
		String colliderEntityId = getEntityA().getID();
		String tilemapEntityId = tilemapEntity.getID();

		// Add tile coordinates to make each tile collision unique
		return colliderEntityId + "_" + tilemapEntityId + "_" + tileX + "_" + tileY;
	}

	/**
	 * Equals method that takes tile coordinates into account.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		TilemapCollisionPair that = (TilemapCollisionPair) o;
		return tileX == that.tileX && tileY == that.tileY;
	}

	/**
	 * Hash code that includes tile coordinates.
	 */
	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + tileX;
		result = 31 * result + tileY;
		return result;
	}
}