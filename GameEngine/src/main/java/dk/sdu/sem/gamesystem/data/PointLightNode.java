package dk.sdu.sem.gamesystem.data;

import dk.sdu.sem.commonsystem.*;
import dk.sdu.sem.gamesystem.components.PointLightComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;

import java.util.Set;

public class PointLightNode extends Node implements INodeProvider<PointLightNode> {
	public TransformComponent transform;
	public PointLightComponent pointLight;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		this.transform = entity.getComponent(TransformComponent.class);
		this.pointLight = entity.getComponent(PointLightComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class, PointLightComponent.class);
	}

	@Override
	public Class<PointLightNode> getNodeType() {
		return PointLightNode.class;
	}

	@Override
	public PointLightNode create() {
		return new PointLightNode();
	}
}