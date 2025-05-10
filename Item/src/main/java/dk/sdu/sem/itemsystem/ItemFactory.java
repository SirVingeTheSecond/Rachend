package dk.sdu.sem.itemsystem;

import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.commonitem.*;
import dk.sdu.sem.commonstats.StatsComponent;
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
	 * Creates an item entity.
	 *
	 * @param position   The position to place the item
	 * @param name       The name of the item
	 * @return The created item entity
	 */
	@Override
	public Entity createItem(Vector2D position, String name) {
		if (colliderFactory.isEmpty()) {
			throw new IllegalStateException("Cannot create item '"+name+"': No IColliderFactory service available");
		}

		Entity item = new Entity();

		IItem pickup = ItemRegistry.getItem(name);

		try {
			// Step 1: Add transform and core components
			TransformComponent transform = new TransformComponent(position, 0, new Vector2D(1.5f, 1.5f));
			item.addComponent(transform);

			// Data component for the item type
			ItemComponent itemComponent = new ItemComponent(pickup);
			item.addComponent(itemComponent);

			// Step 2: Add visuals
			try {
				IAssetReference<Sprite> spriteRef = AssetFacade.createSpriteReference(pickup.getSpriteName());
				SpriteRendererComponent renderer = new SpriteRendererComponent(spriteRef);
				renderer.setRenderLayer(GameConstants.LAYER_OBJECTS);
				item.addComponent(renderer);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Failed to load item: "+name+" sprite: {0}", e.getMessage());
			}

			// Step 3: Add collision capabilities
			CircleColliderComponent collider = colliderFactory.get().addCircleCollider(
				item,
				new Vector2D(0, 0), // Centered offset
				DEFAULT_PICKUP_RADIUS,
				PhysicsLayer.ITEM
			);

			if (collider == null) {
				throw new IllegalStateException("Failed to create collider for item: "+name);
			}

			// MUST be a trigger for item pickup to work
			collider.setTrigger(true);

			if (DEBUG) {
				LOGGER.log(Level.INFO, "Added trigger collider to item: "+name+" (radius: {0})", DEFAULT_PICKUP_RADIUS);
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
			throw new RuntimeException("Failed to create item: " + e.getMessage(), e);
		}
	}

	/**
	 * Creates an item entity from a pool.
	 * @param position The position to place the item.
	 * @param poolName The name of the pool to get the item from.
	 * @return The created item entity, or null if the pool was not found or the pool was empty.
	 */
	@Override
	public Entity createItemFromPool(Vector2D position, String poolName) {
		ItemPool pool = PoolManager.getInstance().getItemPool(poolName);
		if (pool == null) {
			LOGGER.warning("Item pool '"+poolName+"' not found!");
			return null;
		}

		ItemPool.ItemEntry itemEntry = pool.getRandomItem();
		if (itemEntry == null) {
			LOGGER.warning("Item pool '"+poolName+"' is empty!");
			return null;
		}

		return createItem(position, itemEntry.name);
	}

	/**
	 * Applies an item to an entity.
	 * @param entity The entity to apply the item to.
	 * @param name The name of the item to apply.
	 * @return True if the item was successfully applied, false otherwise.
	 */
	@Override
	public boolean applyItem(Entity entity, String name) {
		if (entity.hasComponent(StatsComponent.class))
			return ItemRegistry.getItem(name).applyEffect(entity);
		else
			throw new IllegalStateException("Cannot apply item '"+name+"' to entity without StatsComponent");
	}

	/**
	 * Applies an item to an entity from a pool.
	 * @param entity The entity to apply the item to.
	 * @param poolName The name of the pool to get the item from.
	 * @return True if the item was successfully applied, false otherwise.
	 */
	@Override
	public boolean applyItemFromPool(Entity entity, String poolName) {
		ItemPool pool = PoolManager.getInstance().getItemPool(poolName);
		if (pool == null) {
			LOGGER.warning("Item pool '"+poolName+"' not found!");
			return false;
		}

		ItemPool.ItemEntry itemEntry = pool.getRandomItem();
		if (itemEntry == null) {
			LOGGER.warning("Item pool '"+poolName+"' is empty!");
			return false;
		}
		return applyItem(entity, itemEntry.name);
	}
}