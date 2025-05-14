package dk.sdu.sem.enemysystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.data.RaycastHit;
import dk.sdu.sem.collisionsystem.CollisionServiceFactory;
import dk.sdu.sem.commonpathfinding.IPathfindingSPI;
import dk.sdu.sem.commonpathfinding.IPathfindingTargetProvider;
import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.IWeaponSPI;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * System that updates enemy state and handles enemy movement towards player.
 */
public class EnemySystem implements IUpdate {
	private static final Logging LOGGER = Logging.createLogger("EnemySystem", LoggingLevel.DEBUG);

	private final IPathfindingSPI pathfindingSPI;

	private static final float CLOSE_RANGE_SLOWDOWN = 0.6f;

	public EnemySystem() {
		this.pathfindingSPI = ServiceLoader.load(IPathfindingSPI.class).findFirst().orElse(null);
	}

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
			Vector2D enemyPos = enemyEntity.getComponent(TransformComponent.class).getPosition();

			Vector2D randomMovement = new Vector2D(
				(float) (Math.cos(Math.random() * 2 * Math.PI) * GameConstants.TILE_SIZE * 2),
				(float) (Math.sin(Math.random() * 2 * Math.PI)* GameConstants.TILE_SIZE * 2)
			);

			return Optional.of(enemyPos.add(randomMovement));
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

			// Split into movement and attack logic
			// Movement depends on pathfinding, shooting doesn't
			if (pathfindingSPI != null && node.pathfinding != null) {
				handleEnemyMovement(node, playerNode, playerPos);
			}

			// Always attempt shooting if in range, regardless of pathfinding
			checkAndShoot(node, playerPos, playerNode);
		}

		toRemove.forEach(e -> {
			if (e.getScene() != null) e.getScene().removeEntity(e);
		});
	}

	/**
	 * Gets the collision service instance using the factory.
	 * Using this method ensures we get the proper debug-enabled service
	 * when debug visualization is active.
	 */
	private ICollisionSPI getCollisionService() {
		return CollisionServiceFactory.getService();
	}

	/**
	 * Check if the enemy can shoot at the player and activate weapon if possible
	 */
	private void checkAndShoot(EnemyNode node, Vector2D playerPos, PlayerTargetNode playerNode) {
		Entity enemy = node.getEntity();
		Vector2D enemyPos = node.transform.getPosition();
		Vector2D toPlayer = playerPos.subtract(enemyPos);
		float distance = toPlayer.magnitude();

		// Only attempt to shoot if player is within attack range
		float attackRange = node.stats.getStat(StatType.ATTACK_RANGE) * GameConstants.TILE_SIZE;
		if (distance <= attackRange) {
			// Check if we have line of sight to player using collision
			boolean canSeePlayer = checkDirectLineOfSight(enemyPos, toPlayer, playerNode.getEntity());

			if (canSeePlayer) {
				// We have line of sight, attack the player
				Vector2D direction = toPlayer.normalize();
				IWeaponSPI activeWeapon = node.weapon.getActiveWeapon();
				if (activeWeapon != null) {
					activeWeapon.activateWeapon(enemy, direction);
				}
			}
		}
	}

	/**
	 * Handle enemy movement using pathfinding when available
	 */
	private void handleEnemyMovement(EnemyNode node, PlayerTargetNode playerNode, Vector2D playerPos) {
		Entity enemy = node.getEntity();
		Vector2D enemyPos = node.transform.getPosition();
		Vector2D toPlayer = playerPos.subtract(enemyPos);

		// Ensure LastKnownPositionComponent exists
		LastKnownPositionComponent lastKnown = enemy.ensure(LastKnownPositionComponent.class, LastKnownPositionComponent::new);

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
			followPath(node);
		} else {
			// LOST SIGHT
			handleNoLineOfSight(node, lastKnown, enemyPos);
		}
	}

	/**
	 * Direct line of sight check for shooting logic - uses collision system directly
	 */
	private boolean checkDirectLineOfSight(Vector2D origin, Vector2D dirToPlayer, Entity targetEntity) {
		ICollisionSPI collisionService = getCollisionService();
		if (collisionService == null) return false;

		RaycastHit hit = collisionService.raycast(origin, dirToPlayer, 1000,
			List.of(PhysicsLayer.PLAYER, PhysicsLayer.OBSTACLE));

		return hit.isHit() && hit.getEntity() == targetEntity;
	}

	private boolean checkLineOfSight(Vector2D origin, Vector2D dirToPlayer, PlayerTargetNode playerNode) {
		if (pathfindingSPI != null) {
			return pathfindingSPI.hasLineOfSight(
				origin,
				dirToPlayer,
				playerNode.getEntity(),
				List.of(PhysicsLayer.PLAYER, PhysicsLayer.OBSTACLE)
			);
		}

		ICollisionSPI collisionService = getCollisionService();
		if (collisionService == null) return false;

		RaycastHit hit = collisionService.raycast(origin, dirToPlayer, 1000,
			List.of(PhysicsLayer.PLAYER, PhysicsLayer.OBSTACLE));
		return hit.isHit() && hit.getEntity() == playerNode.getEntity();
	}

	private void handleNoLineOfSight(EnemyNode node,
									 LastKnownPositionComponent lastKnown,
									 Vector2D enemyPos) {
		switch (lastKnown.getState()) {
			case FOLLOWING:
				// just lost sight -> switch to SEARCHING
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
				// start moving around randomly
				followPath(node);
				break;
		}
	}

	private void followPath(EnemyNode node) {
		if (node.pathfinding == null) return;

		if (node.pathfinding.current().isPresent()) {
			Vector2D route = node.pathfinding.current().get();
			Vector2D worldTarget;

			if (pathfindingSPI != null) {
				worldTarget = pathfindingSPI.toWorldPosition(route).add(new Vector2D(0.5f, 0.5f));
			} else {
				worldTarget = toWorldPosition(route).add(new Vector2D(0.5f, 0.5f));
			}

			// reached waypoint?
			if (Vector2D.euclidean_distance(worldTarget, node.transform.getPosition()) < GameConstants.TILE_SIZE) {
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
				Vector2D nextWorldPos;
				if (pathfindingSPI != null) {
					nextWorldPos = pathfindingSPI.toWorldPosition(next).add(new Vector2D(0.5f, 0.5f));
				} else {
					nextWorldPos = toWorldPosition(next).add(new Vector2D(0.5f, 0.5f));
				}

				Vector2D dir = nextWorldPos.subtract(node.transform.getPosition()).normalize();
				moveTowards(node, dir);
			});
		} else {
			LastKnownPositionComponent comp = node.getEntity().getComponent(LastKnownPositionComponent.class);
			if (comp != null) {
				comp.setState(EnemyState.IDLE);
			}
		}
	}

	private void stopMovement(PhysicsComponent phys) {
		phys.setVelocity(Vector2D.ZERO);
	}

	private Vector2D toWorldPosition(Vector2D gridPos) {
		return gridPos.scale((float) GameConstants.TILE_SIZE);
	}

	private void moveTowards(EnemyNode node, Vector2D dir) {
		float speed = node.stats.getMoveSpeed();
		Vector2D delta = dir.scale(speed * (float) Time.getDeltaTime());
		Vector2D vel = node.physics.getVelocity().add(delta);
		node.physics.setVelocity(vel);
	}

	@SuppressWarnings("unused")
	private void slowDown(PhysicsComponent phys) {
		Vector2D v = phys.getVelocity().scale(CLOSE_RANGE_SLOWDOWN);
		phys.setVelocity(v);
	}
}