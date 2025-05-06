package dk.sdu.sem.particlesystem;

import dk.sdu.sem.commonsystem.*;

import java.util.Set;

public class ParticlesNode extends Node implements INodeProvider<ParticlesNode> {
	public ParticleEmitterComponent emitter;
	public TransformComponent transform;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		this.emitter = entity.getComponent(ParticleEmitterComponent.class);
		this.transform = entity.getComponent(TransformComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(ParticleEmitterComponent.class, TransformComponent.class);
	}

	@Override
	public ParticlesNode create() {
		return new ParticlesNode();
	}

	@Override
	public Class<ParticlesNode> getNodeType() {
		return ParticlesNode.class;
	}
}
