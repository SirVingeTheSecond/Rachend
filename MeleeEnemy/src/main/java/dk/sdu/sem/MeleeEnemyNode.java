package dk.sdu.sem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.enemy.EnemyComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;

import java.util.Set;

public class MeleeEnemyNode extends Node  {
	TransformComponent transformComponent;
	PhysicsComponent physicsComponent;
	EnemyComponent enemyComponent;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		transformComponent = entity.getComponent(TransformComponent.class);
		this.physicsComponent = entity.getComponent(PhysicsComponent.class);
		this.enemyComponent = entity.getComponent(EnemyComponent.class);
	}


	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class,PhysicsComponent.class,
			EnemyComponent.class);
	}
}
