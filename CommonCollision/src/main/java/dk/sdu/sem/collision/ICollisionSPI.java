package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Service provider interface for collision detection.
 * Implementation of this interface will be loaded via ServiceLoader.
 */
public interface ICollisionSPI {
	/**
	 * Tests if two colliders are intersecting.
	 *
	 * @param a The first collider
	 * @param b The second collider
	 * @return true if colliders intersect, false otherwise
	 */
	boolean checkCollision(ICollider a, ICollider b);

	/**
	 * Tests if a collider intersects with a tile.
	 *
	 * @param collider The collider to check
	 * @param tileX The tile X coordinate
	 * @param tileY The tile Y coordinate
	 * @param tileSize The size of the tile
	 * @return true if the collider intersects with the tile, false otherwise
	 */
	boolean checkTileCollision(ICollider collider, int tileX, int tileY, int tileSize);

	/**
	 * Tests if a position is valid for movement.
	 *
	 * @param collider The collider to check
	 * @param proposedPosition The proposed new position
	 * @return true if the position is valid (no collisions), false otherwise
	 */
	boolean isPositionValid(ICollider collider, Vector2D proposedPosition);

	/**
	 * Casts a ray and returns information about what it hit.
	 *
	 * @param origin Starting point of the ray
	 * @param direction Direction of the ray
	 * @param maxDistance Maximum distance to check
	 * @return Information about the raycast result
	 */
	RaycastResult raycast(Vector2D origin, Vector2D direction, float maxDistance);
}