package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.Entity;

/**
 * Factory interface for creating collision components.
 */
public interface IColliderFactory {
	/**
	 * Adds a circle collider to an entity.
	 *
	 * @param entity The entity to add the collider to
	 * @param offsetX X offset from entity position
	 * @param offsetY Y offset from entity position
	 * @param radius Radius of the collider
	 * @return True if successful, false otherwise
	 */
	boolean addCircleCollider(Entity entity, float offsetX, float offsetY, float radius);

	/**
	 * Adds a circle collider to an entity with a specific physics layer.
	 *
	 * @param entity The entity to add the collider to
	 * @param offsetX X offset from entity position
	 * @param offsetY Y offset from entity position
	 * @param radius Radius of the collider
	 * @param layer The physics layer for collision filtering
	 * @return True if successful, false otherwise
	 */
	boolean addCircleCollider(Entity entity, float offsetX, float offsetY, float radius, PhysicsLayer layer);

	/**
	 * Adds a tilemap collider to an entity.
	 *
	 * @param entity The entity to add the collider to
	 * @param collisionFlags 2D array of collision flags (1 = solid, 0 = passable)
	 * @return True if successful, false otherwise
	 */
	boolean addTilemapCollider(Entity entity, int[][] collisionFlags);

	/**
	 * Adds a tilemap collider to an entity with a specific physics layer.
	 *
	 * @param entity The entity to add the collider to
	 * @param collisionFlags 2D array of collision flags (1=solid, 0=passable)
	 * @param layer The physics layer for collision filtering
	 * @return True if successful, false otherwise
	 */
	boolean addTilemapCollider(Entity entity, int[][] collisionFlags, PhysicsLayer layer);
}