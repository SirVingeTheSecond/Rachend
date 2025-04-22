package dk.sdu.sem.collisionsystem.nodes;

import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.shapes.GridShape;
import dk.sdu.sem.commonsystem.*;
import dk.sdu.sem.commontilemap.TilemapComponent;

import java.util.Set;

public class TilemapColliderNode extends Node implements INodeProvider<TilemapColliderNode> {
	public TransformComponent transform;
	public TilemapComponent tilemap;
	public ColliderComponent collider;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		transform = entity.getComponent(TransformComponent.class);
		tilemap = entity.getComponent(TilemapComponent.class);
		collider = entity.getComponent(ColliderComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class, TilemapComponent.class, ColliderComponent.class);
	}

	@Override
	public Class<TilemapColliderNode> getNodeType() {
		return TilemapColliderNode.class;
	}

	@Override
	public TilemapColliderNode create() {
		return new TilemapColliderNode();
	}

	public GridShape getGridShape() {
		return (GridShape) collider.getShape();
	}
}