package dk.sdu.sem.gamesystem.data;

import dk.sdu.sem.gamesystem.components.IComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;

import java.util.Set;

public class RenderNode extends Node {
	public TransformComponent transform;
	public SpriteRendererComponent renderer;

	public RenderNode() {

	}

	private RenderNode(TransformComponent transform, SpriteRendererComponent renderer) {
		this.transform = transform;
		this.renderer = renderer;
	};

	@Override
	public boolean matches(Entity entity) {
		return entity.hasComponent(TransformComponent.class) &&
			entity.hasComponent(SpriteRendererComponent.class);
	}

	@Override
	public Node createNode(Entity entity) {
		return new RenderNode(
			entity.getComponent(TransformComponent.class),
			entity.getComponent(SpriteRendererComponent.class));
	}
}
