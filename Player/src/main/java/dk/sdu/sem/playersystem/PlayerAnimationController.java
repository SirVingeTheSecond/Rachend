package dk.sdu.sem.playersystem;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.Set;

/**
 * System that updates player animation parameters based on player state.
 */
public class PlayerAnimationController implements IUpdate {
	@Override
	public void update() {
		Set<PlayerNode> playerNodes = NodeManager.active().getNodes(PlayerNode.class);

		for (PlayerNode node : playerNodes) {
			AnimatorComponent animator = node.getEntity().getComponent(AnimatorComponent.class);
			if (animator == null) continue;

			PhysicsComponent physics = node.physicsComponent;
			SpriteRendererComponent renderer = node.getEntity().getComponent(SpriteRendererComponent.class);

			if (renderer == null) continue;

			Vector2D velocity = physics.getVelocity();
			boolean isMoving = velocity.magnitudeSquared() > 100.0f; // Threshold to avoid flicker

			animator.setParameter("isMoving", isMoving);

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