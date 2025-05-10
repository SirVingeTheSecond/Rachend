package dk.sdu.sem.playersystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.WeaponComponent;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.input.Key;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

import java.util.Set;

/**
 * System responsible for handling player movement based on input.
 */
public class PlayerSystem implements IUpdate {
	private static final Logging LOGGER = Logging.createLogger("PlayerSystem", LoggingLevel.DEBUG);

	@Override
	public void update() {
		Set<PlayerNode> nodes = NodeManager.active().getNodes(PlayerNode.class);

		nodes.forEach(node -> {
			handleMovement(node);
			handleDash(node);
			handleWeapon(node);
		});
	}

	// In PlayerSystem.java
	private void handleDash(PlayerNode node) {
		// Only handle dash particle effects
		if (node.dash.isActivelyDashing() && Time.getFrameCount() % 6 == 0) {
			Vector2D position = node.transform.getPosition().add(Vector2D.DOWN.scale(2f));
			int amount = (int)(node.physics.getVelocity().magnitude() * 0.01f);
			node.emitter.emit(new PlayerDashParticle(position), amount);
		}

		// Animation handling can stay
		node.animator.setParameter("isDashing", node.dash.isActivelyDashing());
	}

	private void handleWeapon(PlayerNode node) {
		if (Input.getKey(Key.MOUSE1)) {
			Entity playerEntity = node.getEntity();
			Vector2D crosshairPosition = Input.getMousePosition();
			Vector2D playerPosition = node.transform.getPosition();
			Vector2D direction = crosshairPosition.subtract(playerPosition).normalize();

			playerEntity.getComponent(WeaponComponent.class).getActiveWeapon().activateWeapon(playerEntity, direction);
		}
	}
         
	private void handleMovement(PlayerNode node) {
		Vector2D move = Input.getMove();

		boolean isInputActive = move.x() != 0 || move.y() != 0;
		node.animator.setParameter("hasInput", isInputActive);

		if (move.x() != 0) {
			// the x axis is mostly non-zero anyways, so this if statement is not really needed
			node.animator.setParameter("inputDirection", move.x());
		}

		float speed = node.stats.getMoveSpeed();
		Vector2D force = move.scale(speed * (float)Time.getDeltaTime());
		node.physics.addImpulse(force);
	}
}