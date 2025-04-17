package dk.sdu.sem.pathfindingsystem;

import dk.sdu.sem.commonsystem.*;

import java.util.Set;

public class PathfindingNode extends Node implements INodeProvider<PathfindingNode> {
	public PathfindingComponent pathfindingComponent;
	public TransformComponent transform;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		this.pathfindingComponent = entity.getComponent(PathfindingComponent.class);
		this.transform = entity.getComponent(TransformComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(PathfindingComponent.class, TransformComponent.class);
	}

	@Override
	public Class<PathfindingNode> getNodeType() {
		return PathfindingNode.class;
	}

	@Override
	public PathfindingNode create() {
		return new PathfindingNode();
	}
}
