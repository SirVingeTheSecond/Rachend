package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.collision.components.BoxColliderComponent;
import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.collision.components.CollisionStateComponent;
import dk.sdu.sem.collision.components.TilemapColliderComponent;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commontilemap.TilemapComponent;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

/**
 * Factory for creating collision components.
 */
public class ColliderFactory implements IColliderFactory {
	private static final Logging LOGGER = Logging.createLogger("ColliderFactory", LoggingLevel.DEBUG);
	// COMPONENT CREATION METHODS

	@Override
	public CircleColliderComponent addCircleCollider(Entity entity, Vector2D offset, float radius, PhysicsLayer layer) {
		try {
			CircleColliderComponent collider = new CircleColliderComponent(
				entity, offset, radius, false, layer
			);
			entity.addComponent(collider);

			if (!entity.hasComponent(CollisionStateComponent.class)) {
				entity.addComponent(new CollisionStateComponent());
			}

			return collider;
		} catch (Exception e) {
			LOGGER.error("Failed to add circle collider: " + e.getMessage());
			return null;
		}
	}

	@Override
	public CircleColliderComponent addCircleCollider(Entity entity, Vector2D offset, float radius, boolean isTrigger, PhysicsLayer layer) {
		try {
			CircleColliderComponent collider = new CircleColliderComponent(
				entity, offset, radius, isTrigger, layer
			);
			entity.addComponent(collider);

			if (!entity.hasComponent(CollisionStateComponent.class)) {
				entity.addComponent(new CollisionStateComponent());
			}

			return collider;
		} catch (Exception e) {
			LOGGER.error("Failed to add circle collider: " + e.getMessage());
			return null;
		}
	}

	@Override
	public BoxColliderComponent addBoxCollider(Entity entity, Vector2D offset, float width, float height, PhysicsLayer layer) {
		try {
			BoxColliderComponent collider = new BoxColliderComponent(
				entity, offset, width, height, false, layer
			);
			entity.addComponent(collider);

			if (!entity.hasComponent(CollisionStateComponent.class)) {
				entity.addComponent(new CollisionStateComponent());
			}

			return collider;
		} catch (Exception e) {
			LOGGER.error("Failed to add box collider: " + e.getMessage());
			return null;
		}
	}

	@Override
	public TilemapColliderComponent addTilemapCollider(Entity entity, int[][] collisionFlags, PhysicsLayer layer) {
		try {
			// Get the tilemap component to determine tile size
			TilemapComponent tilemapComponent = entity.getComponent(TilemapComponent.class);
			if (tilemapComponent == null) {
				// If no component exists, create and add one
				tilemapComponent = new TilemapComponent(
					null,  // No tileset needed for collision
					collisionFlags,
					GameConstants.TILE_SIZE
				);
				entity.addComponent(tilemapComponent);
			}

			// Create the collider component
			TilemapColliderComponent collider = new TilemapColliderComponent(
				entity, tilemapComponent, collisionFlags
			);

			collider.setLayer(layer);
			entity.addComponent(collider);

			if (!entity.hasComponent(CollisionStateComponent.class)) {
				entity.addComponent(new CollisionStateComponent());
			}

			return collider;
		} catch (Exception e) {
			LOGGER.error("Failed to add tilemap collider: " + e.getMessage());
			return null;
		}
	}

	// ENTITY CREATION METHODS

	@Override
	public Entity createCircleColliderEntity(Vector2D position, float radius, PhysicsLayer layer) {
		Entity entity = new Entity();
		entity.addComponent(new TransformComponent(position, 0, new Vector2D(1, 1)));
		addCircleCollider(entity, new Vector2D(0, 0), radius, layer);
		return entity;
	}

	@Override
	public Entity createBoxColliderEntity(Vector2D position, float width, float height, PhysicsLayer layer) {
		Entity entity = new Entity();
		entity.addComponent(new TransformComponent(position, 0, new Vector2D(1, 1)));
		addBoxCollider(entity, new Vector2D(0, 0), width, height, layer);
		return entity;
	}

	@Override
	public Entity createTilemapColliderEntity(Vector2D position, int[][] collisionFlags, PhysicsLayer layer) {
		Entity entity = new Entity();
		entity.addComponent(new TransformComponent(position, 0, new Vector2D(1, 1)));

		// Create tilemap component
		TilemapComponent tilemapComponent = new TilemapComponent(
			null, collisionFlags, GameConstants.TILE_SIZE
		);
		entity.addComponent(tilemapComponent);

		// Add the collider (which will use the tilemap component)
		addTilemapCollider(entity, collisionFlags, layer);

		return entity;
	}

	@Override
	public Entity createCircleTriggerEntity(Vector2D position, float radius, PhysicsLayer layer) {
		Entity entity = new Entity();
		entity.addComponent(new TransformComponent(position, 0, new Vector2D(1, 1)));

		CircleColliderComponent collider = new CircleColliderComponent(
			entity, new Vector2D(0, 0), radius, true, layer  // isTrigger = true
		);
		entity.addComponent(collider);

		return entity;
	}

	@Override
	public Entity createBoxTriggerEntity(Vector2D position, float width, float height, PhysicsLayer layer) {
		Entity entity = new Entity();
		entity.addComponent(new TransformComponent(position, 0, new Vector2D(1, 1)));

		BoxColliderComponent collider = new BoxColliderComponent(
			entity, new Vector2D(0, 0), width, height, true, layer  // isTrigger = true
		);
		entity.addComponent(collider);

		return entity;
	}

	// CONVENIENCE METHODS WITH DEFAULT PARAMETERS

	/**
	 * Adds a circle collider with default layer.
	 */
	public CircleColliderComponent addCircleCollider(Entity entity, Vector2D offset, float radius) {
		return addCircleCollider(entity, offset, radius, PhysicsLayer.DEFAULT);
	}

	/**
	 * Adds a box collider with default layer.
	 */
	public BoxColliderComponent addBoxCollider(Entity entity, Vector2D offset, float width, float height) {
		return addBoxCollider(entity, offset, width, height, PhysicsLayer.DEFAULT);
	}

	/**
	 * Adds a tilemap collider with default obstacle layer.
	 */
	public TilemapColliderComponent addTilemapCollider(Entity entity, int[][] collisionFlags) {
		return addTilemapCollider(entity, collisionFlags, PhysicsLayer.OBSTACLE);
	}
}