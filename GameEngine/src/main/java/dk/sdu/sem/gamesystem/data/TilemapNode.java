package dk.sdu.sem.gamesystem.data;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commontilemap.TilemapComponent;
import dk.sdu.sem.gamesystem.components.TilemapRendererComponent;

import java.util.Set;

/**
 * Node for entities with transform and tilemap components.
 */
public class TilemapNode extends Node {
	public TransformComponent transform;
	public TilemapComponent tilemap;
	public TilemapRendererComponent renderer;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		this.transform = entity.getComponent(TransformComponent.class);
		this.tilemap = entity.getComponent(TilemapComponent .class);
		this.renderer = entity.getComponent(TilemapRendererComponent .class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class, TilemapComponent.class, TilemapRendererComponent.class);
	}
}