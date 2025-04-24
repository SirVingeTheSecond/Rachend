package dk.sdu.sem.props;

import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.INodeProvider;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;

import java.util.Set;

public class BreakableNode extends Node implements INodeProvider<BreakableNode> {
	SpriteRendererComponent renderer;
	PropBreakComponent prop;
	StatsComponent stats;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		renderer = entity.getComponent(SpriteRendererComponent.class);
		prop = entity.getComponent(PropBreakComponent.class);
		stats = entity.getComponent(StatsComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(SpriteRendererComponent.class, PropBreakComponent.class, StatsComponent.class);
	}

	@Override
	public Class<BreakableNode> getNodeType() {
		return BreakableNode.class;
	}

	@Override
	public BreakableNode create() {
		return new BreakableNode();
	}
}
