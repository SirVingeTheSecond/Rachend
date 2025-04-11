package dk.sdu.sem.collision;

import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Main interface for the physics system.
 * Similar to Unity's Physics class.
 */
public interface ICollisionSPI {
	/**
	 * Casts a ray against all colliders in the scene.
	 *
	 * @param origin The starting point of the ray in world coordinates
	 * @param direction The direction of the ray
	 * @param maxDistance The maximum distance the ray should check for collisions
	 * @return Information about what was hit, or null if nothing was hit
	 */
	RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance);

	/**
	 * Casts a ray against colliders in the scene, filtered by layer.
	 *
	 * @param origin The starting point of the ray in world coordinates
	 * @param direction The direction of the ray
	 * @param maxDistance The maximum distance the ray should check for collisions
	 * @param layer The physics layer to cast against
	 * @return Information about what was hit, or null if nothing was hit
	 */
	RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance, PhysicsLayer layer);

	/**
	 * Checks if two entities are colliding.
	 *
	 * @param a The first entity
	 * @param b The second entity
	 * @return True if the entities are colliding, false otherwise
	 */
	boolean checkCollision(Entity a, Entity b);

	/**
	 * Cleans up any collision state associated with an entity.
	 * Should be called when removing an entity from the scene.
	 *
	 * @param entity The entity to clean up
	 */
	void cleanupEntity(Entity entity);

	boolean isPositionValid(ColliderComponent collider, Vector2D proposedPos);
}