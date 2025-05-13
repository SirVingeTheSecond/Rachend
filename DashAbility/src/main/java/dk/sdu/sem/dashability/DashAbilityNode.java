package dk.sdu.sem.dashability;

import dk.sdu.sem.commonparticle.ParticleEmitterComponent;
import dk.sdu.sem.commonsystem.*;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;

import java.util.Set;

public class DashAbilityNode extends Node implements INodeProvider<DashAbilityNode> {
	public PhysicsComponent physics;
	public DashAbilityComponent dash;
	public TransformComponent transform;
	public ParticleEmitterComponent emitter;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		this.physics = entity.getComponent(PhysicsComponent.class);
		this.dash = entity.getComponent(DashAbilityComponent.class);
		this.transform = entity.getComponent(TransformComponent.class);
		this.emitter = entity.getComponent(ParticleEmitterComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(PhysicsComponent.class, DashAbilityComponent.class, TransformComponent.class, ParticleEmitterComponent.class);
	}

	@Override
	public Class<DashAbilityNode> getNodeType() {
		return DashAbilityNode.class;
	}

	@Override
	public DashAbilityNode create() {
		return new DashAbilityNode();
	}
}

