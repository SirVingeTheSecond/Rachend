package dk.sdu.sem.meleeweaponsystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.data.RaycastHit;
import dk.sdu.sem.collisionsystem.CollisionServiceFactory;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonsystem.debug.IDebugDrawManager;
import dk.sdu.sem.commonweapon.WeaponDamage;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import dk.sdu.sem.player.PlayerComponent;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * System for handling melee weapon attacks.
 * Includes debug visualization using IDebugDrawManager.
 */
public class MeleeSystem implements IUpdate {
	private static final Logging LOGGER = Logging.createLogger("MeleeSystem", LoggingLevel.DEBUG);

	private final ICollisionSPI collisionService;
	private final IDebugDrawManager debugDrawManager;

	public MeleeSystem() {
		this.collisionService = CollisionServiceFactory.getService();
		this.debugDrawManager = ServiceLoader.load(IDebugDrawManager.class).findFirst().orElse(null);

		if (collisionService == null) {
			LOGGER.error("MeleeSystem: Failed to load CollisionSPI");
		}

		if (debugDrawManager == null) {
			LOGGER.debug("MeleeSystem: No IDebugDrawManager found - debug visualization will be disabled");
		} else {
			LOGGER.debug("MeleeSystem: IDebugDrawManager found - debug visualization enabled");
		}
	}

	@Override
	public void update() {
		if (collisionService == null) return;

		Set<MeleeEffectNode> meleeNodes = NodeManager.active().getNodes(MeleeEffectNode.class);
		List<Entity> entitiesToRemove = new ArrayList<>();

		for (MeleeEffectNode node : meleeNodes) {
			// Update lifetime timer with delta time
			float deltaTime = (float) Time.getDeltaTime();
			boolean completed = node.meleeEffect.update(deltaTime);

			// Handle animation transition
			if (node.meleeEffect.shouldTriggerStrike()) {
				// Set the parameter to trigger the transition
				node.animator.setParameter("shouldStrike", true);
				node.meleeEffect.setChangedToStrikeAnimation(true);

				// Apply damage when the strike animation starts
				applyDamage(node);
				node.meleeEffect.setHasDealtDamage(true);

				// Visualize the attack if debug draw manager is available
				visualizeAttack(node);
			}

			// Remove when lifetime is over
			if (completed || node.meleeEffect.isCompleted()) {
				entitiesToRemove.add(node.getEntity());
			}
		}

		// Remove expired effects
		for (Entity entity : entitiesToRemove) {
			if (entity.getScene() != null) {
				entity.getScene().removeEntity(entity);
				LOGGER.debug("Removed melee effect: %s", entity.getID());
			}
		}
	}

	private void visualizeAttack(MeleeEffectNode node) {
		if (debugDrawManager == null || !debugDrawManager.isEnabled()) return;

		Vector2D position = node.transform.getPosition();
		float attackRange = node.meleeEffect.getAttackRange();

		// Draw the attack range
		debugDrawManager.drawCircle(position, attackRange, Color.YELLOW.deriveColor(0, 1, 1, 0.4), 0.3f);

		// Show attack direction if available
		if (node.transform.getRotation() != 0) {
			float directionAngle = node.transform.getRotation();
			Vector2D attackDir = new Vector2D(
				(float)Math.cos(Math.toRadians(directionAngle)),
				(float)Math.sin(Math.toRadians(directionAngle))
			).scale(attackRange);

			debugDrawManager.drawRay(position, attackDir, Color.RED, 0.3f);
		}

		// Show text with attack info
		debugDrawManager.drawText("Melee Attack", position.add(new Vector2D(0, -20)), Color.WHITE, 0.3f);
	}

	private void applyDamage(MeleeEffectNode node) {
		Entity owner = node.meleeEffect.getOwner();
		Vector2D position = node.transform.getPosition();
		float attackRange = node.meleeEffect.getAttackRange();

		// Determine which layer to check against based on owner type
		PhysicsLayer targetLayer = owner.hasComponent(PlayerComponent.class) ?
			PhysicsLayer.ENEMY : PhysicsLayer.PLAYER;

		// Use the collision service to find entities within the attack range
		List<Entity> overlappedEntities = collisionService.overlapCircle(position, attackRange, targetLayer);

		if (overlappedEntities.isEmpty()) {
			return;
		}

		// Apply damage to each hit entity that has direct line of sight
		for (Entity entity : overlappedEntities) {
			TransformComponent entityTransform = entity.getComponent(TransformComponent.class);
			if (entityTransform == null) continue;

			Vector2D entityPos = entityTransform.getPosition();
			Vector2D toEntity = entityPos.subtract(position);

			// Perform a raycast to check if there are obstacles between the attacker and the target
			RaycastHit raycastHit = collisionService.raycast(
				position,
				toEntity.normalize(),
				toEntity.magnitude(),
				List.of(PhysicsLayer.OBSTACLE, targetLayer)
			);

			// Log hit information
			LOGGER.debug("Melee raycast result: hit=" + raycastHit.isHit() +
				", entity=" + (raycastHit.getEntity() != null ? raycastHit.getEntity().getID() : "null"));

			// Apply damage if we hit the target entity directly
			if (raycastHit.isHit() && raycastHit.getEntity() == entity) {
				WeaponDamage.applyDamage(entity, 1.0f);
				LOGGER.debug("Melee attack hit entity: {}", entity.getID());

				// Visualize hit if debug draw manager is available
				if (debugDrawManager != null && debugDrawManager.isEnabled()) {
					debugDrawManager.drawCircle(
						entityPos,
						10,
						Color.YELLOW.brighter(),
						0.3f
					);

					debugDrawManager.drawText(
						"HIT!",
						entityPos.add(new Vector2D(0, -15)),
						Color.RED,
						0.3f
					);
				}
			}
		}
	}
}