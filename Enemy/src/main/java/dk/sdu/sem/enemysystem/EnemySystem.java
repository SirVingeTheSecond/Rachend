package dk.sdu.sem.enemysystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.data.RaycastHit;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.enemy.EnemyComponent;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.pathfindingsystem.IPathfindingTargetProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * System that updates enemy state and handles enemy movement towards player.
 */
public class EnemySystem implements IUpdate {
	private static final float CLOSE_RANGE_SLOWDOWN = 0.6f;
	private static final float ATTACK_RANGE = 5.0f;

	/**
	 * Target provider that chooses between current player position (when following)
	 * or last known player position (when searching).
	 */
	private static class StateBasedTargetProvider implements IPathfindingTargetProvider {
		private final Entity enemyEntity;
		private Vector2D playerPosition;

		public StateBasedTargetProvider(Entity enemyEntity, Vector2D playerPosition) {
			this.enemyEntity = enemyEntity;
			this.playerPosition = playerPosition;
		}

		@Override
		public Optional<Vector2D> getTarget() {
			LastKnownPositionComponent lastKnown = enemyEntity.getComponent(LastKnownPositionComponent.class);
			if (lastKnown == null || lastKnown.getState() == EnemyState.FOLLOWING) {
				return Optional.of(playerPosition);
			}
			if (lastKnown.getState() == EnemyState.SEARCHING) {
				return Optional.of(lastKnown.getLastKnownPosition());
			}
			return Optional.empty();
		}
	}

	@Override
	public void update() {
		Set<EnemyNode> enemies = NodeManager.active().getNodes(EnemyNode.class);
		if (enemies.isEmpty()) return;

		PlayerTargetNode playerNode = NodeManager.active()
			.getNodes(PlayerTargetNode.class)
			.stream().findFirst().orElse(null);
		if (playerNode == null) return;

		Vector2D playerPos = playerNode.getEntity()
			.getComponent(TransformComponent.class)
			.getPosition();

		List<Entity> toRemove = new ArrayList<>();
		for (EnemyNode node : enemies) {
			if (node.stats.getCurrentHealth() <= 0) {
				toRemove.add(node.getEntity());
				continue;
			}
			updateEnemyBehavior(node, playerNode, playerPos);
		}
		toRemove.forEach(e -> {
			if (e.getScene() != null) e.getScene().removeEntity(e);
		});
	}

	private void updateEnemyBehavior(EnemyNode node, PlayerTargetNode playerNode, Vector2D playerPos) {
		Entity enemy = node.getEntity();
		Vector2D enemyPos = node.transform.getPosition();
		Vector2D toPlayer = playerPos.subtract(enemyPos);

		// Ensure LastKnownPositionComponent exists
		LastKnownPositionComponent lastKnown = enemy.getComponent(LastKnownPositionComponent.class);
		if (lastKnown == null) {
			lastKnown = new LastKnownPositionComponent();
			enemy.addComponent(lastKnown);
		}

		// Check line of sight
		boolean seesPlayer = checkLineOfSight(enemyPos, toPlayer, playerNode);

		// Install or update target provider
		if (!(node.pathfinding.targetProvider instanceof StateBasedTargetProvider)) {
			node.pathfinding.targetProvider = new StateBasedTargetProvider(enemy, playerPos);
		} else {
			((StateBasedTargetProvider) node.pathfinding.targetProvider).playerPosition = playerPos;
		}

		if (seesPlayer) {
			// FOLLOWING
			lastKnown.setLastKnownPosition(playerPos);
			lastKnown.setState(EnemyState.FOLLOWING);
			followPathAndAttack(node, toPlayer);
		} else {
			// LOST SIGHT
			handleNoLineOfSight(node, lastKnown, enemyPos);
		}
	}

	private void handleNoLineOfSight(EnemyNode node,
									 LastKnownPositionComponent lastKnown,
									 Vector2D enemyPos) {
		switch (lastKnown.getState()) {
			case FOLLOWING:
				// just lost sight → switch to SEARCHING
				lastKnown.setState(EnemyState.SEARCHING);
				// pathfinding system will now aim for lastKnownPosition
				followPath(node);
				break;

			case SEARCHING:
				Vector2D target = lastKnown.getLastKnownPosition();
				float dist = Vector2D.euclidean_distance(enemyPos, target);
				if (dist < GameConstants.TILE_SIZE * 0.5f) {
					// reached last known pos without seeing -> go IDLE
					lastKnown.setState(EnemyState.IDLE);
					stopMovement(node.physics);
				} else {
					// still moving to last known pos
					followPath(node);
				}
				break;

			case IDLE:
				// stayed where you are
				stopMovement(node.physics);
				break;
		}
	}

	private boolean checkLineOfSight(Vector2D origin,
									 Vector2D dirToPlayer,
									 PlayerTargetNode playerNode) {
		ICollisionSPI spi = ServiceLoader.load(ICollisionSPI.class).findFirst().orElse(null);
		if (spi == null) return false;
		RaycastHit hit = spi.raycast(origin, dirToPlayer, 1000,
			List.of(PhysicsLayer.PLAYER, PhysicsLayer.OBSTACLE));
		return hit.isHit() && hit.getEntity() == playerNode.getEntity();
	}

	private void followPathAndAttack(EnemyNode node, Vector2D toPlayer) {
		followPath(node);
		float dist = toPlayer.magnitude();
		if (dist <= GameConstants.TILE_SIZE * ATTACK_RANGE) {
			Vector2D dir = toPlayer.normalize();
			node.weapon.getActiveWeapon().activateWeapon(node.getEntity(), dir);
		}
	}

	private void followPath(EnemyNode node) {
		node.pathfinding.current().ifPresent(route -> {
			Vector2D worldTarget = toWorldPosition(route).add(new Vector2D(0.5f, 0.5f));
			// reached waypoint?
			if (Vector2D.euclidean_distance(worldTarget, node.transform.getPosition()) < GameConstants.TILE_SIZE * 0.5f) {
				node.pathfinding.advance();
				// if last waypoint and searching → idle
				if (node.pathfinding.current().isEmpty()) {
					LastKnownPositionComponent comp =
						node.getEntity().getComponent(LastKnownPositionComponent.class);
					if (comp != null && comp.getState() == EnemyState.SEARCHING) {
						comp.setState(EnemyState.IDLE);
						stopMovement(node.physics);
					}
				}
			}
			// move to current waypoint
			node.pathfinding.current().ifPresent(next -> {
				Vector2D dir = toWorldPosition(next).add(new Vector2D(0.5f, 0.5f))
					.subtract(node.transform.getPosition()).normalize();
				moveTowards(node.physics, node.enemy, dir);
			});
		});
	}

	private void moveTowards(PhysicsComponent phys, EnemyComponent enemy, Vector2D dir) {
		float speed = enemy.getMoveSpeed();
		Vector2D delta = dir.scale(speed * (float) Time.getDeltaTime());
		Vector2D vel = phys.getVelocity().add(delta);
		phys.setVelocity(vel);
	}

	private void stopMovement(PhysicsComponent phys) {
		phys.setVelocity(Vector2D.ZERO);
	}

	private Vector2D toWorldPosition(Vector2D gridPos) {
		return gridPos.scale((float) GameConstants.TILE_SIZE);
	}

	@SuppressWarnings("unused")
	private void slowDown(PhysicsComponent phys) {
		Vector2D v = phys.getVelocity().scale(CLOSE_RANGE_SLOWDOWN);
		phys.setVelocity(v);
	}
}