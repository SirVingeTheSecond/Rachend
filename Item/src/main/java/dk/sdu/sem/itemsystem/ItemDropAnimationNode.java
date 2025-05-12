package dk.sdu.sem.itemsystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.INodeProvider;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;

import java.util.Set;

/**
 * Node for entities with components needed for item drop animation.
 */
public class ItemDropAnimationNode extends Node implements INodeProvider<ItemDropAnimationNode> {
	public TransformComponent transform;
	public PhysicsComponent physics;
	public ItemDropAnimationComponent dropAnimation;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		transform = entity.getComponent(TransformComponent.class);
		physics = entity.getComponent(PhysicsComponent.class);
		dropAnimation = entity.getComponent(ItemDropAnimationComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class, PhysicsComponent.class, ItemDropAnimationComponent.class);
	}

	@Override
	public Class<ItemDropAnimationNode> getNodeType() {
		return ItemDropAnimationNode.class;
	}

	@Override
	public ItemDropAnimationNode create() {
		return new ItemDropAnimationNode();
	}
}