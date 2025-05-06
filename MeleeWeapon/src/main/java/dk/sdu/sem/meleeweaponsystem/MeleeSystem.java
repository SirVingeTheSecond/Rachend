package dk.sdu.sem.meleeweaponsystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.data.RaycastHit;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.WeaponDamage;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import dk.sdu.sem.player.PlayerComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

public class MeleeSystem implements IUpdate {
	private static final Logging LOGGER = Logging.createLogger("MeleeSystem", LoggingLevel.DEBUG);

	private final ICollisionSPI collisionService;

	public MeleeSystem() {
		this.collisionService = ServiceLoader.load(ICollisionSPI.class).findFirst().orElse(null);

		if (collisionService == null) {
			LOGGER.error("MeleeSystem: Failed to load CollisionSPI");
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
			}

			// Remove when lifetime is over (either using the Timer return value or our own check)
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

	private void applyDamage(MeleeEffectNode node) {
		Entity owner = node.meleeEffect.getOwner();
		Vector2D position = node.transform.getPosition();
		float attackRange = node.meleeEffect.getAttackRange();

		// Determine which layer to check against
		PhysicsLayer targetLayer = owner.hasComponent(PlayerComponent.class) ?
			PhysicsLayer.ENEMY : PhysicsLayer.PLAYER;

		// Find entities in attack range
		List<Entity> overlappedEntities = collisionService.overlapCircle(
			position,
			attackRange,
			targetLayer
		);

		if (overlappedEntities.isEmpty()) {
			return;
		}

		// Apply damage to each hit entity
		for (Entity entity : overlappedEntities) {
			TransformComponent entityTransform = entity.getComponent(TransformComponent.class);
			if (entityTransform == null) continue;

			Vector2D entityPos = entityTransform.getPosition();
			Vector2D toEntity = entityPos.subtract(position);

			// Check for obstacles between attacker and target
			RaycastHit raycastHit = collisionService.raycast(
				position,
				toEntity.normalize(),
				toEntity.magnitude(),
				List.of(PhysicsLayer.OBSTACLE)
			);

			// Only apply damage if there are no obstacles in between
			if (!raycastHit.isHit()) {
				WeaponDamage.applyDamage(entity, 1.0f);
				LOGGER.debug("Melee attack hit entity: %s", entity.getID());
			}
		}
	}
}