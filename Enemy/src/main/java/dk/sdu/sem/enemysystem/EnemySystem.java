package dk.sdu.sem.enemysystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.data.RaycastHit;
import dk.sdu.sem.commonsystem.*;
import dk.sdu.sem.enemy.EnemyComponent;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.services.IUpdate;


import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;

/**
 * System that updates enemy state and handles enemy movement towards player.
 */
public class EnemySystem implements IUpdate {
	// Minimum distance enemies should maintain from the player
	//private static final float DEFAULT_MIN_DISTANCE = 20.0f;
	// Slowdown factor when enemy is too close to player
	private static final float CLOSE_RANGE_SLOWDOWN = 0.6f;

	@Override
	public void update() {
		// get all Enemies on active scene
		Set<EnemyNode> enemyNodes =
			NodeManager.active().getNodes(EnemyNode.class);
		if (enemyNodes.isEmpty()) {
			return;
		}

		// temporary code to get the location of the player
		PlayerTargetNode playerNode = NodeManager.active().getNodes(PlayerTargetNode.class).stream().findFirst().orElse(null);
		if (playerNode == null)
			return;

		// we assume there preexists 1 player entity on the active scene.
		Vector2D playerLocationVector =
			playerNode.getEntity().getComponent(TransformComponent.class).getPosition();

		List<Entity> entitiesToRemove = new ArrayList<>();
		for (EnemyNode node : enemyNodes) {
			if (node.stats.getCurrentHealth() <= 0) {
				entitiesToRemove.add(node.getEntity());
				continue;
			}

			Vector2D enemyPosition = node.transform.getPosition();
			Vector2D playerDirectionVector = playerLocationVector.subtract(enemyPosition);

			//Check line of sight
			ICollisionSPI spi = ServiceLoader.load(ICollisionSPI.class).findFirst().orElse(null);
			if (spi != null) {
				RaycastHit hit = spi.raycast(enemyPosition, playerDirectionVector, 1000, List.of(PhysicsLayer.PLAYER, PhysicsLayer.OBSTACLE));
				if (!hit.isHit() || hit.getEntity() != playerNode.getEntity()) {
					continue;
				}
			}

			float distanceToPlayer = playerDirectionVector.magnitude();

			// Normalize direction for consistent movement speed
			Vector2D normalizedDirection = playerDirectionVector.normalize();

			moveTowards(node.physics, node.enemy, normalizedDirection);

			/*
			// Get preferred distance from component or use default
			float preferredDistance = getPreferredDistance(node.enemy);

			// Move towards player if outside preferred distance
			if (distanceToPlayer > preferredDistance) {
				moveTowards(node.physics, node.enemy, normalizedDirection);
			} else {
				// When close to preferred distance, slow down gradually
				slowDown(node.physics);
			}
			*/

			// Always update weapon targeting
			node.weapon.getWeapon().activateWeapon(node.getEntity(), normalizedDirection);
		}

		for (Entity entity : entitiesToRemove) {
			if (entity.getScene() != null) {
				entity.getScene().removeEntity(entity);
			}
		}
	}

	/**
	 * Gets the preferred distance for an enemy.
	 * @param enemyComponent The enemy component
	 * @return The preferred minimum distance to maintain from player
	 */
	/*
	private float getPreferredDistance(EnemyComponent enemyComponent) {
		// Behavior component will take care of this?
		// Use the default for now
		return DEFAULT_MIN_DISTANCE;
	}
	*/

	/**
	 * Moves the enemy towards a target direction.
	 * @param physicsComponent The physics component
	 * @param enemyComponent The enemy component with movement properties
	 * @param direction Normalized direction vector to move in
	 */
	private void moveTowards(PhysicsComponent physicsComponent,
							 EnemyComponent enemyComponent,
							 Vector2D direction) {
		float moveSpeed = enemyComponent.getMoveSpeed();

		// Create movement vector
		Vector2D moveVector = direction
			.scale(moveSpeed * (float)Time.getDeltaTime());
		Vector2D velocity = physicsComponent.getVelocity();
		Vector2D newVelocity = velocity.add(moveVector);

		physicsComponent.setVelocity(newVelocity);
	}

	/**
	 * Gradually slows down the enemy when near the player.
	 * This prevents jerky movement when near the stopping distance.
	 * @param physicsComponent The physics component to slow down
	 */
	private void slowDown(PhysicsComponent physicsComponent) {
		Vector2D velocity = physicsComponent.getVelocity();

		// Apply a drag factor to gradually slow down
		Vector2D newVelocity = velocity.scale(CLOSE_RANGE_SLOWDOWN);

		physicsComponent.setVelocity(newVelocity);
	}
}