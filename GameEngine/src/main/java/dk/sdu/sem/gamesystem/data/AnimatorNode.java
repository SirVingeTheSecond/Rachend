package dk.sdu.sem.gamesystem.data;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;

import java.util.Set;

/**
 * Node for entities that have both Animator and SpriteRenderer components.
 */
public class AnimatorNode extends Node {
	public AnimatorComponent animator;
	public SpriteRendererComponent renderer;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		this.animator = entity.getComponent(AnimatorComponent.class);
		this.renderer = entity.getComponent(SpriteRendererComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(AnimatorComponent.class, SpriteRendererComponent.class);
	}
}
