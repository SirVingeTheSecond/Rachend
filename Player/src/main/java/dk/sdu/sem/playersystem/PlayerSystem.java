package dk.sdu.sem.playersystem;

import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.WeaponComponent;
import dk.sdu.sem.gamesystem.Game;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.input.Key;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import dk.sdu.sem.player.PlayerState;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import dk.sdu.sem.player.PlayerComponent;

import java.util.Set;

/**
 * System responsible for handling player movement based on input.
 */
public class PlayerSystem implements IUpdate {
	private static final Logging LOGGER = Logging.createLogger("PlayerSystem", LoggingLevel.DEBUG);
	private static final Logging LOGGER = Logging.createLogger("PlayerSystem", LoggingLevel.DEBUG);

	private int horizontalMovement;
	private int verticalMovement;

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
		// Apply to all player entities
		for (PlayerNode node : playerNodes) {
			// Check if player input is enabled
			if (!node.player.isInputEnabled()) {
				// Skip input processing but still update animations
				if (node.getEntity().getComponent(AnimatorComponent.class) != null) {
					// Update animation parameters based on current velocity, not input
					updateAnimationFromVelocity(node);
				}
				continue;
			}

			// Process normal input handling
			handleMovement(node, horizontalMovement, verticalMovement);

			Time.after(0.2f, () -> {
				node.player.state = PlayerState.IDLE;
			});
			// Activate weapon when mouse 1 pressed
			if (Input.getKey(Key.MOUSE1)) {
				Entity playerEntity = node.getEntity();
				WeaponComponent weaponComponent = playerEntity.getComponent(WeaponComponent.class);

				if (weaponComponent != null) {
					Vector2D crosshairPosition = Input.getMousePosition();
					Vector2D direction = crosshairPosition.subtract(
						playerEntity.getComponent(TransformComponent.class).getPosition()
					).normalize();

					weaponComponent.getActiveWeapon().activateWeapon(playerEntity, direction);
				}
			}

			if (node.stats.getStat(StatType.CURRENT_HEALTH) <= 0) {
				Game.getInstance().gameOver();
			}
		}

		node.animator.setParameter("isDashing", node.player.state == PlayerState.DASHING);

		if (node.player.state == PlayerState.DASHING) {
			Vector2D position = node.transform.getPosition().add(Vector2D.DOWN.scale(2f));
			int amount = (int)(node.physics.getVelocity().magnitude() * 0.01f);
			node.emitter.emit(new PlayerDashParticle(position), amount);
		}
	/**
	 * Updates animation parameters based on current velocity
	 */
	private void updateAnimationFromVelocity(PlayerNode node) {
		AnimatorComponent animator = node.getEntity().getComponent(AnimatorComponent.class);
		PhysicsComponent physics = node.physicsComponent;

		if (animator == null || physics == null) return;

		Vector2D velocity = physics.getVelocity();
		boolean isMoving = velocity.magnitudeSquared() > 10.0f; // Small threshold

		animator.setParameter("isMoving", isMoving);
		animator.setParameter("hasInput", false); // No active input

		// Update direction parameter if needed
		if (Math.abs(velocity.x()) > 0.1f) {
			animator.setParameter("inputDirection", velocity.x() > 0 ? 1 : -1);
		}
	}

	/**
	 * Converts key states to movement values
	 */
	private void setMovementAxis() {
		int leftMove = Input.getKey(Key.LEFT) ? -1 : 0;
		int rightMove = Input.getKey(Key.RIGHT) ? 1 : 0;
		int downMove = Input.getKey(Key.DOWN) ? 1 : 0;
		int upMove = Input.getKey(Key.UP) ? -1 : 0;

		horizontalMovement = leftMove + rightMove;
		verticalMovement = upMove + downMove;
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