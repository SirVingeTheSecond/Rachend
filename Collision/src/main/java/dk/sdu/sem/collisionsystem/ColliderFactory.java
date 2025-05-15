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
			TilemapComponent tilemapComponent = entity.getComponent(TilemapComponent.class);
			if (tilemapComponent == null) {
				tilemapComponent = new TilemapComponent(
					null,
					collisionFlags,
					GameConstants.TILE_SIZE
				);
				entity.addComponent(tilemapComponent);
			}

			TilemapColliderComponent collider = new TilemapColliderComponent(
				entity,
				tilemapComponent,
				collisionFlags
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

	@Override
	public Entity createCircleColliderEntity(Vector2D position, float radius, PhysicsLayer layer) {
		Entity entity = new Entity();
		entity.addComponent(new TransformComponent(position, 0, new Vector2D(1, 1)));

		CircleColliderComponent collider = addCircleCollider(entity, new Vector2D(0, 0), radius, layer);
		return collider != null ? entity : null;
	}

	@Override
	public Entity createBoxColliderEntity(Vector2D position, float width, float height, PhysicsLayer layer) {
		Entity entity = new Entity();
		entity.addComponent(new TransformComponent(position, 0, new Vector2D(1, 1)));

		BoxColliderComponent collider = addBoxCollider(entity, new Vector2D(0, 0), width, height, layer);
		return collider != null ? entity : null;
	}

	@Override
	public Entity createTilemapColliderEntity(Vector2D position, int[][] collisionFlags, PhysicsLayer layer) {
		Entity entity = new Entity();
		entity.addComponent(new TransformComponent(position, 0, new Vector2D(1, 1)));

		TilemapColliderComponent collider = addTilemapCollider(entity, collisionFlags, layer);
		return collider != null ? entity : null;
	}

	@Override
	public Entity createCircleTriggerEntity(Vector2D position, float radius, PhysicsLayer layer) {
		Entity entity = new Entity();
		entity.addComponent(new TransformComponent(position, 0, new Vector2D(1, 1)));

		CircleColliderComponent collider = addCircleCollider(entity, new Vector2D(0, 0), radius, true, layer);
		return collider != null ? entity : null;
	}

	@Override
	public Entity createBoxTriggerEntity(Vector2D position, float width, float height, PhysicsLayer layer) {
		Entity entity = new Entity();
		entity.addComponent(new TransformComponent(position, 0, new Vector2D(1, 1)));

		BoxColliderComponent collider = new BoxColliderComponent(
			entity, new Vector2D(0, 0), width, height, true, layer
		);
		entity.addComponent(collider);

		return entity;
	}
}