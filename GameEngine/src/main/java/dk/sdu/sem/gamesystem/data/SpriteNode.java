package dk.sdu.sem.gamesystem.data;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;

import java.util.Set;

public class SpriteNode extends Node {
	public TransformComponent transform;
	public SpriteRendererComponent spriteRenderer;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		this.transform = entity.getComponent(TransformComponent.class);
		this.spriteRenderer = entity.getComponent(SpriteRendererComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class, SpriteRendererComponent.class);
	}
}