package dk.sdu.sem.collision;

import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Interface for the collision system.
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
	boolean doesCollide(Entity a, Entity b);

	/**
	 * Tests collision between two colliders and returns contact information.
	 *
	 * @param colliderA The first collider
	 * @param colliderB The second collider
	 * @return ContactPoint if collision detected, null otherwise
	 */
	ContactPoint getCollisionInfo(ColliderComponent colliderA, ColliderComponent colliderB);

	/**
	 * Cleans up any collision state associated with an entity.
	 * Should be called when removing an entity from the scene.
	 *
	 * @param entity The entity to clean up
	 */
	void cleanupEntity(Entity entity);

	/**
	 * Checks if a proposed position is valid for a collider.
	 *
	 * @param collider The collider to check
	 * @param proposedPos The proposed position
	 * @return True if the position is valid, false otherwise
	 */
	boolean isPositionValid(ColliderComponent collider, Vector2D proposedPos);
}