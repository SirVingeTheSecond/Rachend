package dk.sdu.sem;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.INodeProvider;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.enemy.EnemyComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;

import java.util.Set;

public class MeleeEnemyNode extends Node implements INodeProvider<MeleeEnemyNode> {
	TransformComponent transformComponent;
	PhysicsComponent physicsComponent;
	EnemyComponent enemyComponent;

	public MeleeEnemyNode(TransformComponent transformComponent, PhysicsComponent physicsComponent, EnemyComponent enemyComponent) {
		this.transformComponent = transformComponent;
		this.physicsComponent = physicsComponent;
		this.enemyComponent = enemyComponent;
	}

	@Override
	public Class getNodeType() {
		return MeleeEnemyNode.class;
	}

	@Override
	public MeleeEnemyNode create() {
		return (new MeleeEnemyNode(transformComponent,physicsComponent,enemyComponent));
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class,PhysicsComponent.class,
			EnemyComponent.class);
	}
}
