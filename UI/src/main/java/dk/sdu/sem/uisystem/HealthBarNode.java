package dk.sdu.sem.uisystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.INodeProvider;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.player.PlayerComponent;

import java.util.Set;

public class HealthBarNode extends Node implements INodeProvider<HealthBarNode> {

	public StatsComponent stats;
	public PlayerComponent player;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		stats = entity.getComponent(StatsComponent.class);
		player = entity.getComponent(PlayerComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(PlayerComponent.class, StatsComponent.class);
	}

	@Override
	public Class<HealthBarNode> getNodeType() {
		return HealthBarNode.class;
	}

	@Override
	public HealthBarNode create() {
		return new HealthBarNode();
	}
}