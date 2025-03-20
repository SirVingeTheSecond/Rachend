package dk.sdu.sem.playersystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.player.PlayerComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;

import java.util.Set;

public class PlayerNode extends Node {
	public TransformComponent transform;
	public PlayerComponent player;

	@Override
	public void initialize (Entity entity) {
		super.initialize(entity);
		this.transform = entity.getComponent(TransformComponent.class);
		this.player = entity.getComponent(PlayerComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class, PlayerComponent.class);
	}
}
