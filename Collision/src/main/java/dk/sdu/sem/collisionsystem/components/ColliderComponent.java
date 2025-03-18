package dk.sdu.sem.collisionsystem.components;

import dk.sdu.sem.collision.CircleShape;
import dk.sdu.sem.collision.ICollider;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonsystem.Entity;

public class ColliderComponent implements ICollider {

	private final Entity entity;
	private final CircleShape shape;

	public ColliderComponent(Entity entity, Vector2D center, float radius) {
		this.entity = entity;
		this.shape = new CircleShape(center, radius);
	}

	public Entity getEntity() {
		return entity;
	}

	@Override
	public CircleShape getCollisionShape() {
		return shape;
	}
}
