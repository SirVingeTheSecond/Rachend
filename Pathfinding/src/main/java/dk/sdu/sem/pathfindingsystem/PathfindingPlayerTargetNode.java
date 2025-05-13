package dk.sdu.sem.pathfindingsystem;

import dk.sdu.sem.commonpathfinding.PathfindingComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.player.PlayerComponent;

import java.util.Set;

public class PathfindingPlayerTargetNode extends Node {
	public TransformComponent playerTransform;
	public PlayerComponent playerComponent;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		this.playerComponent = entity.getComponent(PlayerComponent.class);
		this.playerTransform = entity.getComponent(TransformComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(PathfindingComponent.class, TransformComponent.class, PlayerComponent.class);
	}
}