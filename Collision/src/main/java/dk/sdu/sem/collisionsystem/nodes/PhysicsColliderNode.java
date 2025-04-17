package dk.sdu.sem.collisionsystem.nodes;

import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.commonsystem.*;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;

import java.util.Set;

public class PhysicsColliderNode extends Node implements INodeProvider<PhysicsColliderNode> {
	public TransformComponent transform;
	public ColliderComponent collider;
	public PhysicsComponent physics;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		transform = entity.getComponent(TransformComponent.class);
		collider = entity.getComponent(ColliderComponent.class);
		physics = entity.getComponent(PhysicsComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class, ColliderComponent.class, PhysicsComponent.class);
	}

	@Override
	public Class<PhysicsColliderNode> getNodeType() {
		return PhysicsColliderNode.class;
	}

	@Override
	public PhysicsColliderNode create() {
		return new PhysicsColliderNode();
	}
}
