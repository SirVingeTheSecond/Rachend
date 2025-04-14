package dk.sdu.sem.collision;

import dk.sdu.sem.collision.components.BoxColliderComponent;
import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.collision.components.TilemapColliderComponent;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Factory interface for creating collision components and entities.
 */
public interface IColliderFactory {
	// COMPONENT CREATION METHODS

	/**
	 * Adds a circle collider to an entity.
	 * @return The created collider component or null if creation failed
	 */
	CircleColliderComponent addCircleCollider(Entity entity, Vector2D offset, float radius, PhysicsLayer layer);

	/**
	 * Adds a box collider to an entity.
	 * @return The created collider component or null if creation failed
	 */
	BoxColliderComponent addBoxCollider(Entity entity, Vector2D offset, float width, float height, PhysicsLayer layer);

	/**
	 * Adds a tilemap collider to an entity.
	 * @return The created collider component or null if creation failed
	 */
	TilemapColliderComponent addTilemapCollider(Entity entity, int[][] collisionFlags, PhysicsLayer layer);

	// ENTITY CREATION METHODS

	/**
	 * Creates an entity with a circle collider.
	 * @return A fully configured entity with transform and collider components
	 */
	Entity createCircleColliderEntity(Vector2D position, float radius, PhysicsLayer layer);

	/**
	 * Creates an entity with a box collider.
	 * @return A fully configured entity with transform and collider components
	 */
	Entity createBoxColliderEntity(Vector2D position, float width, float height, PhysicsLayer layer);

	/**
	 * Creates an entity with a tilemap collider.
	 * @return A fully configured entity with transform, tilemap, and collider components
	 */
	Entity createTilemapColliderEntity(Vector2D position, int[][] collisionFlags, PhysicsLayer layer);

	// TRIGGER VARIANTS

	/**
	 * Creates an entity with a circle trigger.
	 */
	Entity createCircleTriggerEntity(Vector2D position, float radius, PhysicsLayer layer);

	/**
	 * Creates an entity with a box trigger.
	 */
	Entity createBoxTriggerEntity(Vector2D position, float width, float height, PhysicsLayer layer);
}