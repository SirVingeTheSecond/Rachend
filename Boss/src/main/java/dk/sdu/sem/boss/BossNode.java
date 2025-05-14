package dk.sdu.sem.boss;

import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.*;

import java.util.Set;

public class BossNode extends Node implements INodeProvider<BossNode> {
	public TransformComponent transform;
	public BossComponent boss;
	public StatsComponent stats;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		transform = entity.getComponent(TransformComponent.class);
		boss = entity.getComponent(BossComponent.class);
		stats = entity.getComponent(StatsComponent.class);
	}

	@Override
	public Class<BossNode> getNodeType() {
		return BossNode.class;
	}

	@Override
	public BossNode create() {
		return new BossNode();
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class, BossComponent.class, StatsComponent.class);
	}
}
