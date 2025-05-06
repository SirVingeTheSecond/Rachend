package dk.sdu.sem.hitmarkers;

import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.*;

import java.util.Set;

public class HitMarkerNode extends Node implements INodeProvider<HitMarkerNode> {

	public TransformComponent transform;
	public StatsComponent stats;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		this.transform = entity.getComponent(TransformComponent.class);
		this.stats = entity.getComponent(StatsComponent.class);
		HitMarkerSystem.registerNode(this);
	}

	@Override
	public void uninitialize() {
		super.uninitialize();
		HitMarkerSystem.unregisterNode(this);
	}

	@Override
	public Class<HitMarkerNode> getNodeType() {
		return HitMarkerNode.class;
	}

	@Override
	public HitMarkerNode create() {
		return new HitMarkerNode();
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class, StatsComponent.class);
	}
}
