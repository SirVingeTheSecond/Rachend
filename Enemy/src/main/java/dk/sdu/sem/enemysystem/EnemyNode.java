package dk.sdu.sem.enemysystem;

import dk.sdu.sem.enemy.EnemyComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.gamesystem.components.TransformComponent;

import java.util.Set;

public class EnemyNode extends Node {

	//Enemy transform component
	public TransformComponent transform;
	public EnemyComponent enemy;
	public PhysicsComponent physics;

	//Initialize the enemy with a transform component
	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		this.transform = entity.getComponent(TransformComponent.class);
		this.enemy = entity.getComponent(EnemyComponent.class);
		this.physics = entity.getComponent(PhysicsComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class, EnemyComponent.class, PhysicsComponent.class);
	}
}
