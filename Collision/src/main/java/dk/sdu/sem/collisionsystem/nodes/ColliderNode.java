package dk.sdu.sem.collisionsystem.nodes;

import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.commonsystem.*;

import java.util.Set;

/**
 * Node for entities with collider components.
 */
public class ColliderNode extends Node implements INodeProvider<ColliderNode> {
	public TransformComponent transform;
	public ColliderComponent collider;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		transform = entity.getComponent(TransformComponent.class);
		collider = entity.getComponent(ColliderComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class, ColliderComponent.class);
	}

	@Override
	public Class<ColliderNode> getNodeType() {
		return ColliderNode.class;
	}

	@Override
	public ColliderNode create() {
		return new ColliderNode();
	}
}