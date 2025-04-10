package dk.sdu.sem.enemysystem;

import dk.sdu.sem.commonhealth.HealthComponent;
import dk.sdu.sem.enemy.EnemyComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.pathfindingsystem.PathfindingComponent;

import java.util.Set;

public class EnemyNode extends Node {

	//Enemy transform component
	public TransformComponent transform;
	public EnemyComponent enemy;
	public PhysicsComponent physics;
	public HealthComponent health;
	public PathfindingComponent pathfinding;

	//Initialize the enemy with a transform component
	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		this.transform = entity.getComponent(TransformComponent.class);
		this.enemy = entity.getComponent(EnemyComponent.class);
		this.physics = entity.getComponent(PhysicsComponent.class);
		this.health = entity.getComponent(HealthComponent.class);
		this.pathfinding = entity.getComponent(PathfindingComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class, EnemyComponent.class, PhysicsComponent.class, HealthComponent.class, PathfindingComponent.class);
	}
}
