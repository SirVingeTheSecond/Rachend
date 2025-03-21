package dk.sdu.sem.enemy;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

public interface IEnemyFactory {

	/**
	 * Creates an enemy entity with default settings.
	 * @return The created enemy entity
	 */

	Entity create();

	/**
	 * Creates an enemy entity with custom position, speed and friction
	 *
	 * @param position  Starting position for the enemy
	 * @param moveSpeed Movement speed
	 * @param friction  Physics friction coefficient
	 * @param health Health of the enemy
	 * @return The created enemy entity
	 */

	Entity create(Vector2D position, float moveSpeed, float friction, int health);
}