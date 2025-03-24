package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.ColliderComponent;
import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.collision.TilemapColliderComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

public class ColliderFactory implements IColliderFactory {
	@Override
	public boolean addCircleCollider(Entity entity, float offsetX, float offsetY, float radius) {
		ColliderComponent collider = new ColliderComponent(
			entity,
			new Vector2D(offsetX, offsetY),
			radius
		);
		entity.addComponent(collider);
		return true;
	}

	@Override
	public boolean addTilemapCollider(Entity entity, int[][] collisionFlags) {
		TilemapColliderComponent collider = new TilemapColliderComponent(collisionFlags);
		entity.addComponent(collider);
		return true;
	}
}