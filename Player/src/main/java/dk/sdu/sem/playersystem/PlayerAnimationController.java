package dk.sdu.sem.playersystem;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.Set;

/**
 * System that updates player animation and visual representation based on movement state.
 */
public class PlayerAnimationController implements IUpdate {
	// Velocity threshold to consider the player as moving
	private static final float MOVEMENT_THRESHOLD = 100.0f;

	// Direction change threshold to avoid jitter in sprite flipping
	private static final float DIRECTION_CHANGE_THRESHOLD = 0.1f;

	@Override
	public void update() {
		Set<PlayerNode> playerNodes = NodeManager.active().getNodes(PlayerNode.class);

		for (PlayerNode node : playerNodes) {
			updatePlayerVisuals(node);
		}
	}

	/**
	 * Updates all visual aspects of the player based on current state
	 */
	private void updatePlayerVisuals(PlayerNode node) {
		AnimatorComponent animator = node.getEntity().getComponent(AnimatorComponent.class);
		if (animator == null) return;

		SpriteRendererComponent renderer = node.getEntity().getComponent(SpriteRendererComponent.class);
		if (renderer == null) return;

		PhysicsComponent physics = node.physics;
		Vector2D velocity = physics.getVelocity();

		// Determine if player is moving based on actual velocity, not input
		boolean isMoving = velocity.magnitudeSquared() > MOVEMENT_THRESHOLD;

		// Set the movement state for animation transitions
		animator.setParameter("isMoving", isMoving);

		// Handle sprite flipping based on velocity
		updateSpriteDirection(velocity, renderer, animator);

		// Clear one-frame animation parameters
		animator.setParameter("isDashing", false);
	}

	/**
	 * Updates the sprite direction based on velocity and handles sprite flipping
	 */
	private void updateSpriteDirection(Vector2D velocity, SpriteRendererComponent renderer, AnimatorComponent animator) {
		// Only change direction if there's significant horizontal movement
		if (Math.abs(velocity.x()) > DIRECTION_CHANGE_THRESHOLD) {
			boolean isFacingRight = velocity.x() > 0;

			// Flip the sprite based on movement direction
			renderer.setFlipX(!isFacingRight);

			// Set the facing direction for animation logic
			animator.setParameter("facingRight", isFacingRight);
		}

		// For vertical movement, we don't change the horizontal facing
	}
}