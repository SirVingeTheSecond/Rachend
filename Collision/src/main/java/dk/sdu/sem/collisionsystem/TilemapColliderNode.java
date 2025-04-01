package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.TilemapColliderComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.gamesystem.components.TilemapComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;

import java.util.Set;

/**
 * Node for entities with transform, tilemap, and tilemap collider components.
 */
public class TilemapColliderNode extends Node {
	public TransformComponent transform;
	public TilemapComponent tilemap;
	public TilemapColliderComponent tilemapCollider;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		transform = entity.getComponent(TransformComponent.class);
		tilemap = entity.getComponent(TilemapComponent.class);
		tilemapCollider = entity.getComponent(TilemapColliderComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class, TilemapComponent.class, TilemapColliderComponent.class);
	}
}