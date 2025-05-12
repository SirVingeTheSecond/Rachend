package dk.sdu.sem.itemsystem;

import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;

import java.util.Random;
import java.util.function.BiConsumer;

public class ItemDropSystem {
	private static final ItemFactory itemFactory = new ItemFactory();
	private static final Random random = new Random();

	public static void registerDropNode(ItemDropNode dropNode) {
		dropNode.stats.addStatChangeListener(
			StatType.CURRENT_HEALTH,
			new BiConsumer<>() {
				@Override
				public void accept(Float oldValue, Float newValue) {
					if (newValue > 0)
						return;

					if (Math.random() < dropNode.drop.getDropChance()) {
						// Get drop position (centered on entity)
						Vector2D dropPosition = dropNode.transform.getPosition();

						// Calculate ground level based on entity height
						float entityHeight = GameConstants.TILE_SIZE; // Default size

						// Try to get actual entity height from sprite renderer or use default
						SpriteRendererComponent renderer = dropNode.getEntity().getComponent(SpriteRendererComponent.class);
						if (renderer != null && renderer.getSprite() != null) {
							entityHeight = (float) (renderer.getSprite().getSourceRect().getHeight() * dropNode.transform.getScale().y());
						}

						// Calculate ground level exactly as specified
						float groundLevel = dropPosition.y() + (entityHeight * 0.5f);

						// Create item from pool
						Entity item = itemFactory.createItemFromPool(dropPosition, dropNode.drop.getItemPool());

						if (item != null) {
							// Add physics component for movement
							float mass = 1.0f;
							float friction = 0.05f;
							PhysicsComponent physics = new PhysicsComponent(friction, mass);
							item.addComponent(physics);

							// Generate random initial velocity for the throw
							Vector2D initialVelocity = generateRandomThrowVelocity();
							physics.setVelocity(initialVelocity);

							// Add drop animation component with ground level
							ItemDropAnimationComponent dropAnimation = new ItemDropAnimationComponent(initialVelocity, groundLevel);
							item.addComponent(dropAnimation);

							// Add collision handler for bounce effects
							ItemCollisionHandler collisionHandler = new ItemCollisionHandler(item);
							item.addComponent(collisionHandler);

							// Add to scene - must be done AFTER adding components
							dropNode.getEntity().getScene().addEntity(item);
						}
					}

					dropNode.stats.removeStatChangeListener(StatType.CURRENT_HEALTH, this);
				}
			}
		);
	}

	/**
	 * Generates a random initial velocity for the item throw.
	 * Creates a pronounced parabolic arc similar to Binding of Isaac item drops.
	 * @return A Vector2D representing the initial velocity.
	 */
	private static Vector2D generateRandomThrowVelocity() {
		// Random horizontal direction (left or right)
		float xDirection = random.nextFloat() * 2.0f - 1.0f; // Between -1 and 1

		// Initial upward velocity (always up initially)
		float yDirection = -2.0f - random.nextFloat() * 1.5f; // Between -2.0 and -3.5 (stronger upward force)

		// Base velocity magnitude (higher for more pronounced arc)
		float baseVelocity = 100.0f + random.nextFloat() * 50.0f; // Between 100 and 150

		// Create initial velocity vector
		return new Vector2D(xDirection * baseVelocity, yDirection * baseVelocity);
	}
}