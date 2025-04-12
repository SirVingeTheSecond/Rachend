package dk.sdu.sem.itemsystem;

import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.collision.PhysicsLayer;
import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.commonitem.IItemFactory;
import dk.sdu.sem.commonitem.ItemComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.gamesystem.rendering.Sprite;

import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Factory for creating item entities.
 */
public class ItemFactory implements IItemFactory {
	private static final boolean DEBUG = true;

	// IMPORTANT: Use the debugger to get a sense of the size
	private static final float ITEM_COLLIDER_RADIUS = 6.0f;

	@Override
	public Entity create() {
		// Default implementation creates a coin at center of screen
		return createCoin(new Vector2D(400, 300));
	}

	/**
	 * Creates a coin item at the specified position.
	 */
	@Override
	public Entity createCoin(Vector2D position) {
		if (DEBUG) System.out.println("Creating coin item at position: " + position);

		Entity coin = new Entity();

		// Add transform with slightly larger scale for visibility
		coin.addComponent(new TransformComponent(position, 0, new Vector2D(1.5f, 1.5f)));

		// Add item component FIRST - this is important for component checks
		coin.addComponent(new ItemComponent("coin", 1));

		// Add sprite renderer component
		IAssetReference<Sprite> spriteRef = AssetFacade.createSpriteReference("coin");
		SpriteRendererComponent spriteRenderer = new SpriteRendererComponent(spriteRef);
		spriteRenderer.setRenderLayer(GameConstants.LAYER_MIDGROUND);
		coin.addComponent(spriteRenderer);

		Optional<IColliderFactory> optionalFactory = ServiceLoader.load(IColliderFactory.class).findFirst();

		if (optionalFactory.isPresent()) {
			IColliderFactory factory = optionalFactory.get();

			CircleColliderComponent collider = factory.addCircleCollider(
				coin,              // Entity
				new Vector2D(0,0), // Offset
				ITEM_COLLIDER_RADIUS, // Radius
				PhysicsLayer.ITEM   // Layer
			);

			if (collider != null) {
				collider.setTrigger(true);
				if (DEBUG) {
					System.out.println("Added trigger collider to coin (radius: " + ITEM_COLLIDER_RADIUS + ")");
				}
			}
		} else {
			System.out.println("No collision factory available for item");
		}

		// Add trigger handler component
		coin.addComponent(new ItemTrigger(coin));

		return coin;
	}

	/**
	 * Creates a health potion item at the specified position.
	 */
	@Override
	public Entity createHealthPotion(Vector2D position) {
		if (DEBUG) System.out.println("Creating health potion at position: " + position);

		Entity potion = new Entity();

		// Add transform component
		potion.addComponent(new TransformComponent(position, 0, new Vector2D(1.5f, 1.5f)));

		// Add item component FIRST - this is important for component checks
		potion.addComponent(new ItemComponent("health_potion", 1));

		// Add sprite renderer component
		IAssetReference<Sprite> spriteRef = AssetFacade.createSpriteReference("potion");
		SpriteRendererComponent spriteRenderer = new SpriteRendererComponent(spriteRef);
		spriteRenderer.setRenderLayer(GameConstants.LAYER_MIDGROUND);
		potion.addComponent(spriteRenderer);

		Optional<IColliderFactory> optionalFactory = ServiceLoader.load(IColliderFactory.class).findFirst();

		if (optionalFactory.isPresent()) {
			IColliderFactory factory = optionalFactory.get();

			CircleColliderComponent collider = factory.addCircleCollider(
				potion,              // Entity
				new Vector2D(0,0), // Offset
				ITEM_COLLIDER_RADIUS, // Radius
				PhysicsLayer.ITEM   // Layer
			);

			if (collider != null) {
				collider.setTrigger(true);
				if (DEBUG) {
					System.out.println("Added trigger collider to potion (radius: " + ITEM_COLLIDER_RADIUS + ")");
				}
			}
		} else {
			System.out.println("No collision factory available for item");
		}

		return potion;
	}
}