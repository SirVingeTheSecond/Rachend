package dk.sdu.sem.enemysystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.INodeProvider;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.player.PlayerComponent;

import java.util.Set;

public class PlayerTargetNode extends Node implements INodeProvider<PlayerTargetNode> {
	public PlayerComponent player;
	public TransformComponent transform;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		player = entity.getComponent(PlayerComponent.class);
		transform = entity.getComponent(TransformComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(PlayerComponent.class, TransformComponent.class);
	}

	@Override
	public Class<PlayerTargetNode> getNodeType() {
		return PlayerTargetNode.class;
	}

	@Override
	public PlayerTargetNode create() {
		return new PlayerTargetNode();
	}
}
