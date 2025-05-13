package dk.sdu.sem.dashability;

import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.input.Key;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

import java.util.Optional;
import java.util.Set;

public class DashAbilitySystem implements IUpdate {
	private static final Logging LOGGER = Logging.createLogger("DashAbilitySystem", LoggingLevel.DEBUG);

	@Override
	public void update() {
		Set<DashAbilityNode> nodes = NodeManager.active().getNodes(DashAbilityNode.class);

		nodes.forEach(node -> {
			PhysicsComponent physics = node.physics;
			DashAbilityComponent dash = node.dash;
			Entity entity = node.getEntity();
			Optional<AnimatorComponent> animator = Optional.ofNullable(entity.getComponent(AnimatorComponent.class));

			// Get collider component directly from entity
			ColliderComponent collider = entity.getComponent(ColliderComponent.class);

			Vector2D move = Input.getMove();

			// Update dash timer
			dash.update(Time.getDeltaTime());

			// Handle dash start
			if (Input.getKeyDown(Key.SPACE) && !dash.isOnCooldown()) {
				if (move.equals(Vector2D.ZERO)) {
					move = physics.getVelocity();
					if (move.magnitudeSquared() < 1000) {
						return;
					}
					move = move.normalize();
				}

				Vector2D velocity = move.scale(dash.velocityScale);
				dash.use();
				animator.ifPresent(anim -> anim.setParameter("isDashing", true));
				physics.addImpulse(velocity);

				// Enable invincibility by changing physics layer
				if (collider != null) {
					// Store original layer if not already stored
					if (!dash.isInvincibilityActive() && dash.getOriginalLayer() == null) {
						dash.setOriginalLayer(collider.getLayer());
						LOGGER.debug("Stored original physics layer: " + dash.getOriginalLayer());
					}

					// Change to INVINCIBLE layer
					collider.setLayer(PhysicsLayer.INVINCIBLE);
					dash.setInvincibilityActive(true);
					LOGGER.debug("Changed player physics layer to INVINCIBLE for dash");
				}
			}

			// Handle dash end - restore original physics layer
			if (!dash.isActivelyDashing() && dash.isInvincibilityActive() && collider != null) {
				PhysicsLayer originalLayer = dash.getOriginalLayer();
				if (originalLayer != null) {
					collider.setLayer(originalLayer);
					LOGGER.debug("Restored player physics layer to " + originalLayer + " after dash");
				} else {
					collider.setLayer(PhysicsLayer.PLAYER); // Fallback to PLAYER if original wasn't stored
					LOGGER.debug("Restored player physics layer to default PLAYER after dash");
				}
				dash.setInvincibilityActive(false);
			}
		});
	}
}