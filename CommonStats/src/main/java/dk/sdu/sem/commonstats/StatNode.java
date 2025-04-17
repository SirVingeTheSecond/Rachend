package dk.sdu.sem.commonstats;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.INodeProvider;
import dk.sdu.sem.commonsystem.Node;

import java.util.Set;

/**
 * Node for entities with stats component.
 */
public class StatNode extends Node implements INodeProvider<StatNode> {
	public StatsComponent stats;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		stats = entity.getComponent(StatsComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(StatsComponent.class);
	}

	@Override
	public Class<StatNode> getNodeType() {
		return StatNode.class;
	}

	@Override
	public StatNode create() {
		return new StatNode();
	}
}