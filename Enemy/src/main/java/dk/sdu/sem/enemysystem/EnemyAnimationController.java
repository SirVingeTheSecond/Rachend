package dk.sdu.sem.enemysystem;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.Set;

/**
 * System that updates enemy animation parameters based on enemy state.
 */
public class EnemyAnimationController implements IUpdate {
	@Override
	public void update() {
		// Get all enemy nodes
		Set<EnemyNode> enemyNodes = NodeManager.active().getNodes(EnemyNode.class);

		for (EnemyNode node : enemyNodes) {
			// Get the animator component
			AnimatorComponent animator = node.getEntity().getComponent(AnimatorComponent.class);
			if (animator == null) continue;

			PhysicsComponent physics = node.physics;
			SpriteRendererComponent renderer = node.getEntity().getComponent(SpriteRendererComponent.class);

			if (renderer == null) continue;

			// Determine animation state based on velocity
			Vector2D velocity = physics.getVelocity();
			boolean isMoving = velocity.magnitudeSquared() > 100.0f; // Threshold to avoid flicker

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