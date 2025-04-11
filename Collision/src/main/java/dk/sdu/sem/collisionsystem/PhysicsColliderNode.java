package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.commonsystem.TransformComponent;

import java.util.Set;

/**
 * Node for entities with transform, physics, and collider components.
 */
public class PhysicsColliderNode extends Node {
	public TransformComponent transform;
	public PhysicsComponent physicsComponent;
	public ColliderComponent collider;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		transform = entity.getComponent(TransformComponent.class);
		physicsComponent = entity.getComponent(PhysicsComponent.class);
		collider = entity.getComponent(ColliderComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class, PhysicsComponent.class, ColliderComponent.class);
	}
}