package dk.sdu.sem.playersystem;

import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.WeaponComponent;
import dk.sdu.sem.gamesystem.Game;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.input.Key;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import dk.sdu.sem.player.PlayerComponent;

import java.util.Set;

/**
 * System responsible for handling player movement based on input.
 */
public class PlayerSystem implements IUpdate {
	private static final Logging LOGGER = Logging.createLogger("PlayerSystem", LoggingLevel.DEBUG);

	private int horizontalMovement;
	private int verticalMovement;

	// Track dash state for animation purposes
	private boolean isDashing = false;

	@Override
	public void update() {
		// Get all player nodes from the active scene
		Set<PlayerNode> playerNodes = NodeManager.active().getNodes(PlayerNode.class);

		if (playerNodes.isEmpty()) {
			return;
		}

		setMovementAxis();

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

		// Reset dash state after one frame
		isDashing = false;
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

	/**
	 * Applies movement to the physics component based on input
	 */
	private void handleMovement(PlayerNode node, float xMove, float yMove) {
		PhysicsComponent physics = node.physicsComponent;
		PlayerComponent player = node.player;
		AnimatorComponent animator = node.getEntity().getComponent(AnimatorComponent.class);

		boolean isInputActive = xMove != 0 || yMove != 0;

		// Update input parameters for animation
		if (animator != null) {
			// Only update the input direction parameter when input changes
			if (xMove != 0) {
				animator.setParameter("inputDirection", xMove);
			}

			// Set an input active parameter - different from isMoving which is velocity-based
			animator.setParameter("hasInput", isInputActive);
		}

		// Skip physics update if no input
		if (!isInputActive) return;

		float moveSpeed = player.getMoveSpeed();

		// Create movement vector
		Vector2D moveVector = new Vector2D(xMove, yMove)
			.normalize()
			.scale(moveSpeed * (float)Time.getDeltaTime());

		// Apply to physics
		Vector2D velocity = physics.getVelocity();
		Vector2D newVelocity = velocity.add(moveVector);


		// Handle dash
		if (Input.getKeyDown(Key.SPACE)) {
			newVelocity = newVelocity.add(
				new Vector2D(xMove, yMove)
					.normalize()
					.scale(1000)
			);

			isDashing = true;

			// Notify animator about dash
			if (animator != null) {
				animator.setParameter("isDashing", true);
			}
		}

		physics.setVelocity(newVelocity);
	}

	/**
	 * Returns whether the player is currently dashing
	 */
	public boolean isDashing() {
		return isDashing;
	}
}