package dk.sdu.sem.gamesystem.data;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.gamesystem.components.TilemapComponent;
import dk.sdu.sem.commonsystem.TransformComponent;

import java.util.Set;

/**
 * Node for entities with transform and tilemap components.
 */
public class TilemapNode extends Node {
	public TransformComponent transform;
	public TilemapComponent tilemap;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		this.transform = entity.getComponent(TransformComponent.class);
		this.tilemap = entity.getComponent(TilemapComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class, TilemapComponent.class);
	}
}