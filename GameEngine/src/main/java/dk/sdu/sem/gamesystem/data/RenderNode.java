package dk.sdu.sem.gamesystem.nodes;

import dk.sdu.sem.gamesystem.components.IComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.data.Entity;

import java.util.Set;

/**
 * Node for entities that can be rendered (have transform and sprite components)
 */
public class RenderNode implements INode {
	// Component references for convenient access
	public TransformComponent transform;
	public SpriteRendererComponent renderer;

	// The entity this node represents
	private Entity entity;

	/**
	 * Default constructor for node template creation via NodeFactory
	 */
	public RenderNode() {
		// This constructor is used by the NodeFactory to create template nodes
	}

	/**
	 * Constructor for creating node instances tied to specific entities.
	 * @param entity The entity to create a node for, must have required components
	 * @throws IllegalArgumentException if the entity doesn't have all required components
	 */
	public RenderNode(Entity entity) {
		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null");
		}

		// Validate that entity has all required components
		for (Class<? extends IComponent> componentClass : getRequiredComponents()) {
			if (!entity.hasComponent(componentClass)) {
				throw new IllegalArgumentException(
					"Entity must have all required components. Missing: " + componentClass.getSimpleName());
			}
		}

		this.entity = entity;
		this.transform = entity.getComponent(TransformComponent.class);
		this.renderer = entity.getComponent(SpriteRendererComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class, SpriteRendererComponent.class);
	}

	@Override
	public Entity getEntity() {
		return entity;
	}
}