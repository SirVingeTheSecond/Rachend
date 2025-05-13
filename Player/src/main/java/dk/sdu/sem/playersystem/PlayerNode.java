package dk.sdu.sem.playersystem;

import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.player.PlayerComponent;

import java.util.Set;

public class PlayerNode extends Node {
	public TransformComponent transform;
	public PlayerComponent player;
	public PhysicsComponent physics;
	public StatsComponent stats;
	public AnimatorComponent animator;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		this.transform = entity.getComponent(TransformComponent.class);
		this.player = entity.getComponent(PlayerComponent.class);
		this.physics = entity.getComponent(PhysicsComponent.class);
		this.stats = entity.getComponent(StatsComponent.class);
		this.animator = entity.getComponent(AnimatorComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(
			TransformComponent.class,
			PlayerComponent.class,
			PhysicsComponent.class,
			StatsComponent.class,
			AnimatorComponent.class
		);
	}
}