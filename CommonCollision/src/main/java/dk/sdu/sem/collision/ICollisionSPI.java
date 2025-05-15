package dk.sdu.sem.collision;

import dk.sdu.sem.collision.data.CollisionOptions;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.data.RaycastHit;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.List;

/**
 * Service Provider Interface for the collision system.
 */
public interface ICollisionSPI {
	/**
	 * Casts a ray against all colliders in the scene.
	 *
	 * @param origin The starting point of the ray
	 * @param direction The direction of the ray (will be normalized)
	 * @param maxDistance The maximum distance to check
	 * @return Information about what was hit, or a no-hit result
	 */
	RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance);

	/**
	 * Casts a ray against colliders in a specific layer.
	 *
	 * @param origin The starting point of the ray
	 * @param direction The direction of the ray (will be normalized)
	 * @param maxDistance The maximum distance to check
	 * @param layer The physics layer to check against
	 * @return Information about what was hit, or a no-hit result
	 */
	RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance, PhysicsLayer layer);

	/**
	 * Casts a ray against colliders in a list of layers.
	 *
	 * @param origin The starting point of the ray
	 * @param direction The direction of the ray (will be normalized)
	 * @param maxDistance The maximum distance to check
	 * @param layers The physics layers to check against
	 * @return Information about what was hit, or a no-hit result
	 */
	RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance, List<PhysicsLayer> layers);

	/**
	 * Casts a ray and returns all hits along the ray path, sorted by distance.
	 *
	 * @param origin The starting point of the ray
	 * @param direction The direction of the ray
	 * @param maxDistance The maximum distance to check
	 * @return List of hit information sorted by distance (empty if no hits)
	 */
	List<RaycastHit> raycastAll(Vector2D origin, Vector2D direction, float maxDistance);

	/**
	 * Casts a ray against all colliders in specific layers and returns all hits.
	 *
	 * @param origin The starting point of the ray
	 * @param direction The direction of the ray
	 * @param maxDistance The maximum distance to check
	 * @param layers The physics layers to check against
	 * @return List of hit information sorted by distance (empty if no hits)
	 */
	List<RaycastHit> raycastAll(Vector2D origin, Vector2D direction, float maxDistance, List<PhysicsLayer> layers);

	/**
	 * Non-allocating variant of raycast. Fills the provided hitInfo object.
	 *
	 * @param origin The starting point of the ray
	 * @param direction The direction of the ray
	 * @param maxDistance The maximum distance to check
	 * @param hitInfo Object to fill with hit information
	 * @return True if the ray hit something, false otherwise
	 */
	boolean raycast(Vector2D origin, Vector2D direction, float maxDistance, RaycastHit hitInfo);

	/**
	 * Checks if a point is inside any collider.
	 *
	 * @param point The point to check
	 * @return True if the point is inside a collider, false otherwise
	 */
	boolean isPointInCollider(Vector2D point);

	/**
	 * Checks if a point is inside a specific collider.
	 *
	 * @param point The point to check
	 * @param entity The entity with the collider to check
	 * @return True if the point is inside the collider, false otherwise
	 */
	boolean isPointInCollider(Vector2D point, Entity entity);

	/**
	 * Validates if a proposed position is valid for entity movement.
	 *
	 * @param entity The entity to check
	 * @param proposedPosition The position to validate
	 * @param collisionOptions Options to control collision validation behavior
	 * @return True if the position is valid, false otherwise
	 */
	boolean isPositionValid(Entity entity, Vector2D proposedPosition, CollisionOptions collisionOptions);

	// Backward compatibility
	default boolean isPositionValid(Entity entity, Vector2D proposedPosition) {
		return isPositionValid(entity, proposedPosition, CollisionOptions.preventAll(false));
	}

	/**
	 * Gets all entities that overlap a circle.
	 *
	 * @param center The center of the circle
	 * @param radius The radius of the circle
	 * @return A list of entities that overlap the circle
	 */
	List<Entity> overlapCircle(Vector2D center, float radius);

	/**
	 * Gets all entities that overlap a circle in a specific layer.
	 *
	 * @param center The center of the circle
	 * @param radius The radius of the circle
	 * @param layer The physics layer to check against
	 * @return A list of entities that overlap the circle
	 */
	List<Entity> overlapCircle(Vector2D center, float radius, PhysicsLayer layer);

	/**
	 * Gets all entities that overlap a box.
	 *
	 * @param center The center of the box
	 * @param width The width of the box
	 * @param height The height of the box
	 * @return A list of entities that overlap the box
	 */
	List<Entity> overlapBox(Vector2D center, float width, float height);

	/**
	 * Gets all entities that overlap a box in a specific layer.
	 *
	 * @param center The center of the box
	 * @param width The width of the box
	 * @param height The height of the box
	 * @param layer The physics layer to check against
	 * @return A list of entities that overlap the box
	 */
	List<Entity> overlapBox(Vector2D center, float width, float height, PhysicsLayer layer);

	/**
	 * Marks an entity for cleanup in the collision system.
	 * Call this when an entity is destroyed to ensure proper cleanup.
	 *
	 * @param entity The entity to clean up
	 */
	void cleanupEntity(Entity entity);
}