package dk.sdu.sem.enemysystem;

import dk.sdu.sem.health.HealthComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.enemy.EnemyComponent;
import dk.sdu.sem.enemy.IEnemyFactory;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;

public class EnemyFactory implements IEnemyFactory {

	/**
	 * Creates an enemy entity with default settings.
	 */
	@Override
	public Entity create() {
		return create(new Vector2D(500,400), 200.0f, 5.0f, 50);
	}

	/**
	 * Creates an enemy entity with custom settings.
	 */
	@Override
	public Entity create(Vector2D position, float moveSpeed, float friction, int health) {
		Entity enemy = new Entity();
		enemy.addComponent(new TransformComponent(position, 0, new Vector2D(2,2)));
		enemy.addComponent(new PhysicsComponent(friction));
		enemy.addComponent(new EnemyComponent(moveSpeed));
		enemy.addComponent(new HealthComponent(health));

		return enemy;
	}
}
