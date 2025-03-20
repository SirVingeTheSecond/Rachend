package dk.sdu.sem.gamesystem.data;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.gamesystem.components.TileMapComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;

import java.util.Set;

public class TileMapNode extends Node {
	public TransformComponent transform;
	public TileMapComponent tileMap;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		this.transform = entity.getComponent(TransformComponent.class);
		this.tileMap = entity.getComponent(TileMapComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class, TileMapComponent.class);
	}
}