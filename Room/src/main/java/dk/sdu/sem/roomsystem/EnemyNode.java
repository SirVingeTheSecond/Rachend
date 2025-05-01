package dk.sdu.sem.roomsystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.INodeProvider;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.enemy.EnemyComponent;

import java.util.Set;

public class EnemyNode extends Node implements INodeProvider<EnemyNode> {
	EnemyComponent enemyComponent;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		enemyComponent = entity.getComponent(EnemyComponent.class);
	}

	@Override
	public Class<EnemyNode> getNodeType() {
		return EnemyNode.class;
	}

	@Override
	public EnemyNode create() {
		return new EnemyNode();
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(EnemyComponent.class);
	}
}
