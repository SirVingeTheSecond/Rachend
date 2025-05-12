package dk.sdu.sem.itemsystem;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.Set;

/**
 * System that handles the physics and lifecycle of dropped item animations.
 */
public class ItemDropAnimationSystem implements IUpdate {
	private static final float GRAVITY = 300.0f; // Increased gravity for better parabolic motion
	private static final float MIN_VELOCITY_THRESHOLD = 0.1f;
	private static final float MAX_ANIMATION_TIME = 5.0f;
	private static final float GROUND_COLLISION_THRESHOLD = 0.1f;

	@Override
	public void update() {
		float deltaTime = (float) Time.getDeltaTime();
		Set<ItemDropAnimationNode> nodes = NodeManager.active().getNodes(ItemDropAnimationNode.class);

		for (ItemDropAnimationNode node : nodes) {
			TransformComponent transform = node.transform;
			PhysicsComponent physics = node.physics;
			ItemDropAnimationComponent anim = node.dropAnimation;

			// If we've already settled, clean up and skip
			if (!anim.isAnimating()) {
				cleanup(node);
				continue;
			}

			// 1) Progress the timer
			anim.updateTimeAlive(deltaTime);

			// 2) Read current state
			Vector2D position = transform.getPosition();
			Vector2D velocity = physics.getVelocity();
			float groundY = anim.getGroundLevel();

			// 3) Check for ground collision (falling downwards past ground)
			boolean hasHitGround = position.y() >= groundY - GROUND_COLLISION_THRESHOLD
				&& velocity.y() > 0;

			if (hasHitGround) {
				if (anim.canBounce() && velocity.magnitude() > MIN_VELOCITY_THRESHOLD) {
					// Reflect and dampen bounce
					Vector2D bounceVel = new Vector2D(
						velocity.x() * 0.8f,
						-velocity.y() * anim.getBounceFactor()
					).scale(0.9f + (float)(Math.random() * 0.2f));

					physics.setVelocity(bounceVel);
					anim.incrementBounceCount();

					// Snap to just above the ground to avoid tunneling
					transform.setPosition(new Vector2D(position.x(), groundY - GROUND_COLLISION_THRESHOLD));
				} else {
					// Settle: stop moving and end animation
					anim.setAnimating(false);
					physics.setVelocity(Vector2D.ZERO);
					transform.setPosition(new Vector2D(position.x(), groundY - GROUND_COLLISION_THRESHOLD));
					cleanup(node);
				}
			} else {
				// 4) In flight: apply gravity
				physics.addForce(new Vector2D(0, GRAVITY * physics.getMass()));
			}

			// 5) Timeout / too slow -> force end
			if (velocity.magnitude() < MIN_VELOCITY_THRESHOLD || anim.getTimeAlive() > MAX_ANIMATION_TIME)
			{
				anim.setAnimating(false);
				physics.setVelocity(Vector2D.ZERO);
				cleanup(node);
			}
		}
	}

	private void cleanup(ItemDropAnimationNode node) {
		if (node.getEntity().hasComponent(ItemDropAnimationComponent.class)) {
			node.getEntity().removeComponent(ItemDropAnimationComponent.class);
		}

		if (node.getEntity().hasComponent(ItemCollisionHandler.class)) {
			node.getEntity().removeComponent(ItemCollisionHandler.class);
		}

		if (node.getEntity().hasComponent(PhysicsComponent.class)) {
			node.getEntity().removeComponent(PhysicsComponent.class);
		}
	}
}