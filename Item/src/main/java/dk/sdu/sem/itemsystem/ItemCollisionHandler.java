package dk.sdu.sem.itemsystem;

import dk.sdu.sem.collision.ICollisionListener;
import dk.sdu.sem.collision.events.CollisionEnterEvent;
import dk.sdu.sem.collision.events.CollisionExitEvent;
import dk.sdu.sem.collision.events.CollisionStayEvent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;

/**
 * Collision handler that implements bouncing for dropped items.
 */
public class ItemCollisionHandler implements IComponent, ICollisionListener {
	private static final float MIN_BOUNCE_VELOCITY = 10.0f;
	private static final float ITEM_HEIGHT = GameConstants.TILE_SIZE * 0.5f;
	private final Entity itemEntity;

	public ItemCollisionHandler(Entity itemEntity) {
		this.itemEntity = itemEntity;
	}

	@Override
	public void onCollisionEnter(CollisionEnterEvent event) {
		// Make sure this is for our item
		if (event.getEntity() != itemEntity) {
			return;
		}

		ItemDropAnimationComponent dropAnimation = itemEntity.getComponent(ItemDropAnimationComponent.class);
		PhysicsComponent physics = itemEntity.getComponent(PhysicsComponent.class);

		if (dropAnimation == null || physics == null || !dropAnimation.isAnimating()) {
			return;
		}

		// Get collision normal and current velocity
		Vector2D normal = event.getContact().getNormal();
		Vector2D velocity = physics.getVelocity();
		float velocityMagnitude = velocity.magnitude();

		// Only bounce if we have enough velocity and bounce count allows
		if (dropAnimation.canBounce() && velocityMagnitude > MIN_BOUNCE_VELOCITY) {
			// Calculate reflection vector - standard physics reflection
			float dot = velocity.dot(normal);
			Vector2D reflection = velocity.subtract(normal.scale(2.0f * dot));

			// Apply bounce factor to reduce energy with each bounce
			float bounceFactor = dropAnimation.getBounceFactor();
			Vector2D newVelocity = reflection.scale(bounceFactor);

			// Apply a bit of randomness to make bounces feel more natural
			float randomFactor = 0.9f + (float)(Math.random() * 0.2f);
			newVelocity = newVelocity.scale(randomFactor);

			// Set the new velocity
			physics.setVelocity(newVelocity);

			// Increment bounce count
			dropAnimation.incrementBounceCount();
		} else {
			// If velocity is too low or we're out of bounces, stop animating
			dropAnimation.setAnimating(false);
			physics.setVelocity(new Vector2D(0, 0));

			// Make sure item is positioned correctly against the collision surface
			Vector2D position = itemEntity.getComponent(TransformComponent.class).getPosition();
			Vector2D contactPoint = event.getContact().getPoint();

			// Slightly adjust position to ensure no embedded collision
			Vector2D adjustment = normal.scale(5.0f); // Small adjustment along normal
			itemEntity.getComponent(TransformComponent.class).setPosition(position.add(adjustment));
		}
	}

	@Override
	public void onCollisionStay(CollisionStayEvent event) {
		// No processing needed for stay events
	}

	@Override
	public void onCollisionExit(CollisionExitEvent event) {
		// No processing needed for exit events
	}
}