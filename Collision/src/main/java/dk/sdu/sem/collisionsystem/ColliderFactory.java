package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.collision.PhysicsLayer;
import dk.sdu.sem.collision.components.BoxColliderComponent;
import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.collision.components.TilemapColliderComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commontilemap.TilemapComponent;

/**
 * Factory for creating collision components.
 * Now provides a unified approach to creating all types of colliders.
 */
public class ColliderFactory implements IColliderFactory {
	@Override
	public boolean addCircleCollider(Entity entity, float offsetX, float offsetY, float radius) {
		return addCircleCollider(entity, offsetX, offsetY, radius, PhysicsLayer.DEFAULT);
	}

	@Override
	public boolean addCircleCollider(Entity entity, float offsetX, float offsetY, float radius, PhysicsLayer layer) {
		try {
			CircleColliderComponent collider = new CircleColliderComponent(
				entity,
				new Vector2D(offsetX, offsetY),
				radius,
				false,
				layer
			);
			entity.addComponent(collider);
			return true;
		} catch (Exception e) {
			System.err.println("Failed to add circle collider: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Adds a box collider to an entity.
	 *
	 * @param entity The entity to add the collider to
	 * @param offsetX X offset from entity position
	 * @param offsetY Y offset from entity position
	 * @param width Width of the box
	 * @param height Height of the box
	 * @return True if successful, false otherwise
	 */
	public boolean addBoxCollider(Entity entity, float offsetX, float offsetY, float width, float height) {
		return addBoxCollider(entity, offsetX, offsetY, width, height, PhysicsLayer.DEFAULT);
	}

	/**
	 * Adds a box collider to an entity with a specific physics layer.
	 *
	 * @param entity The entity to add the collider to
	 * @param offsetX X offset from entity position
	 * @param offsetY Y offset from entity position
	 * @param width Width of the box
	 * @param height Height of the box
	 * @param layer The physics layer for collision filtering
	 * @return True if successful, false otherwise
	 */
	public boolean addBoxCollider(Entity entity, float offsetX, float offsetY, float width, float height, PhysicsLayer layer) {
		try {
			BoxColliderComponent collider = new BoxColliderComponent(
				entity,
				new Vector2D(offsetX, offsetY),
				width,
				height,
				false,
				layer
			);
			entity.addComponent(collider);
			return true;
		} catch (Exception e) {
			System.err.println("Failed to add box collider: " + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean addTilemapCollider(Entity entity, int[][] collisionFlags) {
		return addTilemapCollider(entity, collisionFlags, PhysicsLayer.OBSTACLE);
	}

	@Override
	public boolean addTilemapCollider(Entity entity, int[][] collisionFlags, PhysicsLayer layer) {
		try {
			// Get the tilemap component to determine tile size
			TilemapComponent tilemapComponent = entity.getComponent(TilemapComponent.class);
			if (tilemapComponent == null) {
				System.err.println("Failed to add tilemap collider: No TilemapComponent found");
				return false;
			}

			// Create a tilemap collider that uses the GridShape internally
			TilemapColliderComponent collider = new TilemapColliderComponent(
				entity,
				tilemapComponent,
				collisionFlags
			);
			collider.setLayer(layer);
			entity.addComponent(collider);
			return true;
		} catch (Exception e) {
			System.err.println("Failed to add tilemap collider: " + e.getMessage());
			return false;
		}
	}
}