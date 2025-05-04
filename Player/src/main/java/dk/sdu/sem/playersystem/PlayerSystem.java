package dk.sdu.sem.playersystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.WeaponComponent;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.input.Key;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import dk.sdu.sem.particlesystem.Particle;
import dk.sdu.sem.particlesystem.ParticleEmitterComponent;
import dk.sdu.sem.player.PlayerComponent;
import dk.sdu.sem.player.PlayerState;

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

	private void handleDash(PlayerNode node) {
		if (Input.getKeyDown(Key.SPACE) && node.player.state != PlayerState.DASHING) {
			node.player.state = PlayerState.DASHING;

			Vector2D dashDirection = Input.getMove();
			node.physics.addImpulse(dashDirection.scale(1000f));

			Time.after(0.2f, () -> {
				node.player.state = PlayerState.IDLE;
			});
		}

		node.animator.setParameter("isDashing", node.player.state == PlayerState.DASHING);

		if (node.player.state == PlayerState.DASHING) {
			Vector2D position = node.transform.getPosition().add(Vector2D.DOWN.scale(2f));
			int amount = (int)(node.physics.getVelocity().magnitude() * 0.01f);
			node.emitter.emit(new PlayerStepParticle(position), amount);
		}
	}

	private void handleWeapon(PlayerNode node) {
		if (Input.getKey(Key.MOUSE1)) {
			Entity playerEntity = node.getEntity();
			Vector2D crosshairPosition = Input.getMousePosition();
			Vector2D playerPosition = node.transform.getPosition();
			Vector2D direction = crosshairPosition.subtract(playerPosition).normalize();

			playerEntity.getComponent(WeaponComponent.class).getWeapon().activateWeapon(playerEntity, direction);
		}
	}

	private void handleMovement(PlayerNode node) {
		Vector2D move = Input.getMove(); // TODO: Replace with actual input handling

		boolean isInputActive = move.x() != 0 || move.y() != 0;
		node.animator.setParameter("hasInput", isInputActive);

		if (move.x() != 0) {
			// the x axis is mostly non-zero anyways, so this if statement is not really needed
			node.animator.setParameter("inputDirection", move.x());
		}

		float speed = node.player.getMoveSpeed();
		Vector2D force = move.scale(speed * (float)Time.getDeltaTime());
		node.physics.addImpulse(force);
	}
}