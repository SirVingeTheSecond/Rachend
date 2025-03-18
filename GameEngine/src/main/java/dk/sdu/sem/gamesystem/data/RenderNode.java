package dk.sdu.sem.gamesystem.data;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.commonsystem.Node;

import java.util.Set;

/**
 * Node for entities that can be rendered (has transform and sprite components)
 */
public class RenderNode extends Node {
	public TransformComponent transform;
	public SpriteRendererComponent renderer;

	/**
	 * Constructor for creating node instances tied to specific entities.
	 * @param entity The entity to create a node for, must have required components
	 * @throws IllegalArgumentException if the entity doesn't have all required components
	 */
	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		this.transform = entity.getComponent(TransformComponent.class);
		this.renderer = entity.getComponent(SpriteRendererComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class, SpriteRendererComponent.class);
	}
}