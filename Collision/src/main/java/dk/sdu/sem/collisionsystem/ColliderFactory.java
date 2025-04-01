package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.collision.PhysicsLayer;
import dk.sdu.sem.collision.components.TilemapColliderComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Factory for creating collision components.
 */
public class ColliderFactory implements IColliderFactory {
	@Override
	public boolean addCircleCollider(Entity entity, float offsetX, float offsetY, float radius) {
		return addCircleCollider(entity, offsetX, offsetY, radius, PhysicsLayer.DEFAULT);
	}

	@Override
	public boolean addCircleCollider(Entity entity, float offsetX, float offsetY, float radius, PhysicsLayer layer) {
		try {
			ColliderComponent collider = new ColliderComponent(
				entity,
				new Vector2D(offsetX, offsetY),
				radius,
				layer
			);
			entity.addComponent(collider);
			return true;
		} catch (Exception e) {
			System.err.println("Failed to add circle collider: " + e.getMessage());
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
			TilemapColliderComponent collider = new TilemapColliderComponent(collisionFlags);
			collider.setLayer(layer);
			entity.addComponent(collider);
			return true;
		} catch (Exception e) {
			System.err.println("Failed to add tilemap collider: " + e.getMessage());
			return false;
		}
	}
}