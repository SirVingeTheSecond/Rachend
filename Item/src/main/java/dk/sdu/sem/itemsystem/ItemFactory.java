package dk.sdu.sem.itemsystem;

import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.commonitem.IItemFactory;
import dk.sdu.sem.commonitem.ItemComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.assets.references.SpriteReference;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.gamesystem.rendering.Sprite;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Logger;

/**
 * Factory for creating item entities.
 */
public class ItemFactory implements IItemFactory {
	private static final Logger LOGGER = Logger.getLogger(ItemFactory.class.getName());
	private static final boolean DEBUG = true;

	// Default config
	private static final float DEFAULT_PICKUP_RADIUS = 8.0f;
	private static final float DEFAULT_COIN_VALUE = 1.0f;
	private static final float DEFAULT_HEALTH_VALUE = 1.0f;

	private final Optional<IColliderFactory> colliderFactory;

	/**
	 * Creates a new item factory and loads required services.
	 *
	 * @throws RuntimeException if critical services cannot be loaded
	 */
	public ItemFactory() {
		this.colliderFactory = ServiceLoader.load(IColliderFactory.class).findFirst();

		if (colliderFactory.isEmpty()) {
			LOGGER.warning("No IColliderFactory implementation found! Item entities will not have colliders.");
		}
	}

	/**
	 * Creates a default item (a coin at a default position).
	 * Implements IItemFactory interface method.
	 *
	 * @return The created item entity
	 */
	@Override
	public Entity create() {
		// Default implementation creates a coin at center of screen
		return createCoin(new Vector2D(400, 300));
	}

	/**
	 * Creates a coin item entity with all required components.
	 * Implements IItemFactory interface method.
	 *
	 * @param position The position to place the coin
	 * @param value The value of the coin (defaults to 1 if <= 0)
	 * @return The created coin entity
	 * @throws RuntimeException if required components cannot be created
	 */
	@Override
	public Entity createCoin(Vector2D position, float value) {
		if (colliderFactory.isEmpty()) {
			throw new IllegalStateException("Cannot create coin: No IColliderFactory service available");
		}

		// Ensure value is positive
		float coinValue = value <= 0 ? DEFAULT_COIN_VALUE : value;

		Entity coin = new Entity();

		try {
			// Step 1: Add core components
			// Add transform with slightly larger scale for visibility
			TransformComponent transform = new TransformComponent(position, 0, new Vector2D(1.5f, 1.5f));
			coin.addComponent(transform);

			// Add item component
			ItemComponent itemComponent = new ItemComponent("coin", (int)coinValue);
			coin.addComponent(itemComponent);

			// Step 2: Add visual components
			try {
				IAssetReference<Sprite> spriteRef = new SpriteReference("coin");
				SpriteRendererComponent renderer = new SpriteRendererComponent(spriteRef);
				renderer.setRenderLayer(GameConstants.LAYER_MIDGROUND);
				coin.addComponent(renderer);
			} catch (Exception e) {
				LOGGER.warning("Coin sprite not found: " + e.getMessage());
			}

			// Step 3: Add collision components
			CircleColliderComponent collider = colliderFactory.get().addCircleCollider(
				coin,
				new Vector2D(0, 0), // Centered offset
				DEFAULT_PICKUP_RADIUS,
				PhysicsLayer.ITEM
			);

			if (collider == null) {
				throw new IllegalStateException("Failed to create collider for coin");
			}

			// Make it a trigger
			collider.setTrigger(true);

			if (DEBUG) {
				LOGGER.info("Added trigger collider to coin (radius: " + DEFAULT_PICKUP_RADIUS + ")");
			}

			// Step 4: Add pickup behavior component
			PickupComponent pickupComponent = new PickupComponent(coin, "coin", coinValue);
			coin.addComponent(pickupComponent);

			return coin;

		} catch (Exception e) {
			// Clean up any partially created entity
			if (coin.getScene() != null) {
				coin.getScene().removeEntity(coin);
			}
			throw new RuntimeException("Failed to create coin: " + e.getMessage(), e);
		}
	}

	/**
	 * Creates a coin item at the default value of 1.
	 * Implements IItemFactory interface method.
	 *
	 * @param position The position to place the coin
	 * @return The created coin entity
	 */
	@Override
	public Entity createCoin(Vector2D position) {
		return createCoin(position, DEFAULT_COIN_VALUE);
	}

	/**
	 * Creates a health potion item with all required components.
	 * Implements IItemFactory interface method.
	 *
	 * @param position The position to place the health potion
	 * @param value The amount of health to restore (defaults to 1 if <= 0)
	 * @return The created health potion entity
	 * @throws RuntimeException if required components cannot be created
	 */
	@Override
	public Entity createHealthPotion(Vector2D position, float value) {
		if (colliderFactory.isEmpty()) {
			throw new IllegalStateException("Cannot create health potion: No IColliderFactory service available");
		}

		// Ensure value is positive
		float healthValue = value <= 0 ? DEFAULT_HEALTH_VALUE : value;

		Entity potion = new Entity();

		try {
			// Step 1: Add core components
			// Add transform component
			TransformComponent transform = new TransformComponent(position, 0, new Vector2D(1.5f, 1.5f));
			potion.addComponent(transform);

			// Add item component
			ItemComponent itemComponent = new ItemComponent("health_potion", (int)healthValue);
			potion.addComponent(itemComponent);

			// Step 2: Add visual components
			try {
				IAssetReference<Sprite> spriteRef = new SpriteReference("potion");
				SpriteRendererComponent renderer = new SpriteRendererComponent(spriteRef);
				renderer.setRenderLayer(GameConstants.LAYER_MIDGROUND);
				potion.addComponent(renderer);
			} catch (Exception e) {
				LOGGER.warning("Health potion sprite not found: " + e.getMessage());
			}

			// Step 3: Add collision components
			CircleColliderComponent collider = colliderFactory.get().addCircleCollider(
				potion,
				new Vector2D(0, 0), // Centered offset
				DEFAULT_PICKUP_RADIUS,
				PhysicsLayer.ITEM
			);

			if (collider == null) {
				throw new IllegalStateException("Failed to create collider for health potion");
			}

			// Make it a trigger
			collider.setTrigger(true);

			if (DEBUG) {
				LOGGER.info("Added trigger collider to health potion (radius: " + DEFAULT_PICKUP_RADIUS + ")");
			}

			// Step 4: Add pickup behavior component
			PickupComponent pickupComponent = new PickupComponent(potion, "health", healthValue);
			potion.addComponent(pickupComponent);

			return potion;

		} catch (Exception e) {
			// Clean up any partially created entity
			if (potion.getScene() != null) {
				potion.getScene().removeEntity(potion);
			}
			throw new RuntimeException("Failed to create health potion: " + e.getMessage(), e);
		}
	}

	/**
	 * Creates a health potion with the default value of 1.
	 * Implements IItemFactory interface method.
	 *
	 * @param position The position to place the health potion
	 * @return The created health potion entity
	 */
	@Override
	public Entity createHealthPotion(Vector2D position) {
		return createHealthPotion(position, DEFAULT_HEALTH_VALUE);
	}

	/**
	 * Creates a custom item entity with all required components.
	 * This is a generic method for creating any type of item with custom properties.
	 *
	 * @param position The position to place the item
	 * @param itemType The type of item (used for identification and behavior)
	 * @param value The value or amount of the item
	 * @param spriteName The name of the sprite to use (can be null for no sprite)
	 * @return The created item entity
	 * @throws RuntimeException if required components cannot be created
	 */
	public Entity createCustomItem(Vector2D position, String itemType, float value, String spriteName) {
		if (colliderFactory.isEmpty()) {
			throw new IllegalStateException("Cannot create custom item: No IColliderFactory service available");
		}

		Entity item = new Entity();

		try {
			// Step 1: Add core components
			// Add transform component
			TransformComponent transform = new TransformComponent(position, 0, new Vector2D(1.5f, 1.5f));
			item.addComponent(transform);

			// Add item component
			ItemComponent itemComponent = new ItemComponent(itemType, (int)value);
			item.addComponent(itemComponent);

			// Step 2: Add visual components if sprite name is provided
			if (spriteName != null && !spriteName.isEmpty()) {
				try {
					IAssetReference<Sprite> spriteRef = new SpriteReference(spriteName);
					SpriteRendererComponent renderer = new SpriteRendererComponent(spriteRef);
					renderer.setRenderLayer(GameConstants.LAYER_MIDGROUND);
					item.addComponent(renderer);
				} catch (Exception e) {
					LOGGER.warning("Sprite '" + spriteName + "' not found: " + e.getMessage());
				}
			}

			// Step 3: Add collision components
			CircleColliderComponent collider = colliderFactory.get().addCircleCollider(
				item,
				new Vector2D(0, 0), // Centered offset
				DEFAULT_PICKUP_RADIUS,
				PhysicsLayer.ITEM
			);

			if (collider == null) {
				throw new IllegalStateException("Failed to create collider for custom item");
			}

			// Make it a trigger
			collider.setTrigger(true);

			if (DEBUG) {
				LOGGER.info("Added trigger collider to " + itemType + " (radius: " + DEFAULT_PICKUP_RADIUS + ")");
			}

			// Step 4: Add pickup behavior component
			PickupComponent pickupComponent = new PickupComponent(item, itemType, value);
			item.addComponent(pickupComponent);

			return item;

		} catch (Exception e) {
			// Clean up any partially created entity
			if (item.getScene() != null) {
				item.getScene().removeEntity(item);
			}
			throw new RuntimeException("Failed to create custom item: " + e.getMessage(), e);
		}
	}
}