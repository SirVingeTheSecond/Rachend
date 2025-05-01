package dk.sdu.sem.itemsystem;

import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.commonitem.IItemFactory;
import dk.sdu.sem.commonitem.ItemComponent;
import dk.sdu.sem.commonitem.PickupComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.assets.references.SpriteReference;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.rendering.Sprite;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating item entities.
 */
public class ItemFactory implements IItemFactory {
	private static final Logger LOGGER = Logger.getLogger(ItemFactory.class.getName());
	private static final boolean DEBUG = false;

	// Default configuration
	private static final float DEFAULT_PICKUP_RADIUS = 12.0f;
	private static final float DEFAULT_COIN_VALUE = 1.0f;
	private static final float DEFAULT_HEALTH_VALUE = 1.0f;

	private final Optional<IColliderFactory> colliderFactory;

	/**
	 * Creates a new item factory and loads required services.
	 */
	public ItemFactory() {
		this.colliderFactory = ServiceLoader.load(IColliderFactory.class).findFirst();

		if (colliderFactory.isEmpty()) {
			LOGGER.warning("No IColliderFactory implementation found! Item entities will not have colliders.");
		}
	}

	/**
	 * Creates a default item entity.
	 * Default implementation typically creates a coin at a default position.
	 */
	@Override
	public Entity create() {
		// Default is a coin at center of screen
		return createCoin(new Vector2D(400, 300));
	}

	/**
	 * Creates a coin item at the specified position.
	 *
	 * @param position Position to place the coin
	 * @return The created coin entity
	 */
	@Override
	public Entity createCoin(Vector2D position) {
		return createCoin(position, DEFAULT_COIN_VALUE);
	}

	/**
	 * Creates a coin item with a specific value.
	 *
	 * @param position Position to place the coin
	 * @param value Value of the coin
	 * @return The created coin entity
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
			// Step 1: Add transform and core components
			TransformComponent transform = new TransformComponent(position, 0, new Vector2D(1.5f, 1.5f));
			coin.addComponent(transform);

			// Data component for the item type
			ItemComponent itemComponent = new ItemComponent("coin", (int)coinValue);
			coin.addComponent(itemComponent);

			// Data component for pickup behavior
			PickupComponent pickupComponent = new PickupComponent("coin", coinValue);
			coin.addComponent(pickupComponent);

			// Step 2: Add visuals
			try {
				IAssetReference<Sprite> spriteRef = AssetFacade.createSpriteReference("coin");
				SpriteRendererComponent renderer = new SpriteRendererComponent(spriteRef);
				renderer.setRenderLayer(GameConstants.LAYER_OBJECTS);
				coin.addComponent(renderer);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Failed to load coin sprite: {0}", e.getMessage());
			}

			// Step 3: Add collision capabilities
			CircleColliderComponent collider = colliderFactory.get().addCircleCollider(
				coin,
				new Vector2D(0, 0), // Centered offset
				DEFAULT_PICKUP_RADIUS,
				PhysicsLayer.ITEM
			);

			if (collider == null) {
				throw new IllegalStateException("Failed to create collider for coin");
			}

			// MUST be a trigger for item pickup to work
			collider.setTrigger(true);

			if (DEBUG) {
				LOGGER.log(Level.INFO, "Added trigger collider to coin (radius: {0})", DEFAULT_PICKUP_RADIUS);
			}

			// Step 4: Add trigger listener for pickup behavior
			PickupTriggerListener triggerListener = new PickupTriggerListener(coin);
			coin.addComponent(triggerListener);

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
	 * Creates a health potion with the default value of 1.
	 *
	 * @param position The position to place the health potion
	 * @return The created health potion entity
	 */
	@Override
	public Entity createHealthPotion(Vector2D position) {
		return createHealthPotion(position, DEFAULT_HEALTH_VALUE);
	}

	/**
	 * Creates a health potion with a specific healing value.
	 *
	 * @param position Position to place the health potion
	 * @param healAmount Amount of health to restore
	 * @return The created health potion entity
	 */
	@Override
	public Entity createHealthPotion(Vector2D position, float healAmount) {
		if (colliderFactory.isEmpty()) {
			throw new IllegalStateException("Cannot create health potion: No IColliderFactory service available");
		}

		// Ensure value is positive
		float healthValue = healAmount <= 0 ? DEFAULT_HEALTH_VALUE : healAmount;

		Entity potion = new Entity();

		try {
			// Step 1: Add transform and core components
			TransformComponent transform = new TransformComponent(position, 0, new Vector2D(1.5f, 1.5f));
			potion.addComponent(transform);

			// Data component for the item type
			ItemComponent itemComponent = new ItemComponent("health_potion", (int)healthValue);
			potion.addComponent(itemComponent);

			// Data component for pickup behavior
			PickupComponent pickupComponent = new PickupComponent("health", healthValue);
			potion.addComponent(pickupComponent);

			// Step 2: Add visuals
			try {
				IAssetReference<Sprite> spriteRef = AssetFacade.createSpriteReference("potion");
				SpriteRendererComponent renderer = new SpriteRendererComponent(spriteRef);
				renderer.setRenderLayer(GameConstants.LAYER_OBJECTS);
				potion.addComponent(renderer);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Failed to load potion sprite: {0}", e.getMessage());
			}

			// Step 3: Add collision capabilities
			CircleColliderComponent collider = colliderFactory.get().addCircleCollider(
				potion,
				new Vector2D(0, 0), // Centered offset
				DEFAULT_PICKUP_RADIUS,
				PhysicsLayer.ITEM
			);

			if (collider == null) {
				throw new IllegalStateException("Failed to create collider for health potion");
			}

			// MUST be a trigger for item pickup to work
			collider.setTrigger(true);

			if (DEBUG) {
				LOGGER.log(Level.INFO, "Added trigger collider to health potion (radius: {0})", DEFAULT_PICKUP_RADIUS);
			}

			// Step 4: Add trigger listener for pickup behavior
			PickupTriggerListener triggerListener = new PickupTriggerListener(potion);
			potion.addComponent(triggerListener);

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
	 * Creates a custom item entity with all required components.
	 * This is a generic method for creating any type of item with custom properties.
	 *
	 * @param position The position to place the item
	 * @param itemType The type of item (used for identification and behavior)
	 * @param value The value or amount of the item
	 * @param spriteName The name of the sprite to use (can be null for no sprite)
	 * @return The created item entity
	 */
	public Entity createCustomItem(Vector2D position, String itemType, float value, String spriteName) {
		if (colliderFactory.isEmpty()) {
			throw new IllegalStateException("Cannot create custom item: No IColliderFactory service available");
		}

		Entity item = new Entity();

		try {
			// Step 1: Add transform and core components
			TransformComponent transform = new TransformComponent(position, 0, new Vector2D(1.5f, 1.5f));
			item.addComponent(transform);

			// Data component for the item type
			ItemComponent itemComponent = new ItemComponent(itemType, (int)value);
			item.addComponent(itemComponent);

			// Data component for pickup behavior
			PickupComponent pickupComponent = new PickupComponent(itemType, value);
			item.addComponent(pickupComponent);

			// Step 2: Add visual representation if sprite name is provided
			if (spriteName != null && !spriteName.isEmpty()) {
				try {
					IAssetReference<Sprite> spriteRef = new SpriteReference(spriteName);
					SpriteRendererComponent renderer = new SpriteRendererComponent(spriteRef);
					renderer.setRenderLayer(GameConstants.LAYER_OBJECTS);
					item.addComponent(renderer);
				} catch (Exception e) {
					LOGGER.log(Level.WARNING, "Sprite '{0}' not found: {1}", new Object[]{spriteName, e.getMessage()});
				}
			}

			// Step 3: Add collision capabilities
			CircleColliderComponent collider = colliderFactory.get().addCircleCollider(
				item,
				new Vector2D(0, 0), // Centered offset
				DEFAULT_PICKUP_RADIUS,
				PhysicsLayer.ITEM
			);

			if (collider == null) {
				throw new IllegalStateException("Failed to create collider for custom item");
			}

			// MUST be a trigger for item pickup to work
			collider.setTrigger(true);

			if (DEBUG) {
				LOGGER.log(Level.INFO, "Added trigger collider to {0} (radius: {1})",
					new Object[]{itemType, DEFAULT_PICKUP_RADIUS});
			}

			// Step 4: Add trigger listener for pickup behavior
			PickupTriggerListener triggerListener = new PickupTriggerListener(item);
			item.addComponent(triggerListener);

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
