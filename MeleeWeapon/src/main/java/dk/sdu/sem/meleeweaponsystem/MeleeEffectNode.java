package dk.sdu.sem.meleeweaponsystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.INodeProvider;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;

import java.util.Set;

public class MeleeEffectNode extends Node implements INodeProvider<MeleeEffectNode> {
	public TransformComponent transform;
	public AnimatorComponent animator;
	public MeleeEffectComponent meleeEffect;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		transform = entity.getComponent(TransformComponent.class);
		animator = entity.getComponent(AnimatorComponent.class);
		meleeEffect = entity.getComponent(MeleeEffectComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(
			TransformComponent.class,
			AnimatorComponent.class,
			MeleeEffectComponent.class
		);
	}

	@Override
	public Class<MeleeEffectNode> getNodeType() {
		return MeleeEffectNode.class;
	}

	@Override
	public MeleeEffectNode create() {
		return new MeleeEffectNode();
	}
}