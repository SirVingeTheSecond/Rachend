package dk.sdu.sem.playersystem;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.assets.AssetManager;
import dk.sdu.sem.gamesystem.assets.SpriteReference;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.Map;
import java.util.Set;

/**
 * System that updates player animation parameters based on player state.
 * Works with AnimatorComponent instead of directly manipulating animations.
 */
public class PlayerAnimationController implements IUpdate {
	@Override
	public void update() {
		// Get all player nodes
		Set<PlayerNode> playerNodes = NodeManager.active().getNodes(PlayerNode.class);

		for (PlayerNode node : playerNodes) {
			// Get the animator component
			AnimatorComponent animator = node.getEntity().getComponent(AnimatorComponent.class);
			if (animator == null) continue;

			PhysicsComponent physics = node.physicsComponent;
			SpriteRendererComponent renderer = node.getEntity().getComponent(SpriteRendererComponent.class);

			if (renderer == null) continue;

			// Determine animation state based on velocity
			Vector2D velocity = physics.getVelocity();
			boolean isMoving = velocity.magnitudeSquared() > 0.5f; // Small threshold to avoid flicker

			// Update animator parameters
			animator.setParameter("isMoving", isMoving);

			// Update sprite flipping based on horizontal movement direction
			if (velocity.getX() < -0.1f) {
				renderer.setFlipX(true);
				animator.setParameter("facingRight", false);
			} else if (velocity.getX() > 0.1f) {
				renderer.setFlipX(false);
				animator.setParameter("facingRight", true);
			}
		}
	}
}