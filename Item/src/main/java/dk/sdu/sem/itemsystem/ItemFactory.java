package dk.sdu.sem.itemsystem;

import dk.sdu.sem.collision.PhysicsLayer;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.commonitem.IItemFactory;
import dk.sdu.sem.commonitem.ItemComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.rendering.Sprite;

/**
 * Factory for creating item entities.
 */
public class ItemFactory implements IItemFactory {
	private static final boolean DEBUG = true;

	// IMPORTANT: Use smaller collider size to prevent accidental collisions
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
		spriteRenderer.setRenderLayer(GameConstants.LAYER_OBJECTS);
		coin.addComponent(spriteRenderer);

		// Add trigger collider - IMPORTANT: isTrigger=true makes this a trigger collider
		ColliderComponent collider = new ColliderComponent(
			coin,             // Entity
			new Vector2D(0,0), // Offset
			ITEM_COLLIDER_RADIUS, // Radius - smaller to avoid overlap with other items
			true,             // isTrigger - THIS MUST BE TRUE!
			PhysicsLayer.ITEM  // PhysicsLayer
		);
		coin.addComponent(collider);

		if (DEBUG) {
			System.out.println("DEBUG COIN CREATION:");
			System.out.println("- Coin ID: " + coin.getID());
			System.out.println("- Has ItemComponent: " + coin.hasComponent(ItemComponent.class));
			System.out.println("- Has ColliderComponent: " + (collider != null));
			if (collider != null) {
				System.out.println("- Collider isTrigger: " + collider.isTrigger());
				System.out.println("- Collider layer: " + collider.getLayer());
				System.out.println("- Collider radius: " + ITEM_COLLIDER_RADIUS);
			}
		}

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
		spriteRenderer.setRenderLayer(GameConstants.LAYER_OBJECTS);
		potion.addComponent(spriteRenderer);

		// Add collider as trigger - IMPORTANT: isTrigger=true makes this a trigger collider
		ColliderComponent collider = new ColliderComponent(
			potion,            // Entity
			new Vector2D(0,0), // Offset
			ITEM_COLLIDER_RADIUS, // Radius - smaller to avoid overlap with other items
			true,              // isTrigger - THIS MUST BE TRUE!
			PhysicsLayer.ITEM   // PhysicsLayer
		);
		potion.addComponent(collider);

		if (DEBUG) {
			System.out.println("Created health potion:");
			System.out.println("  - Entity ID: " + potion.getID());
			System.out.println("  - Position: " + position);
			System.out.println("  - Item type: health_potion (value: 1)");
			System.out.println("  - Collider: radius=" + ITEM_COLLIDER_RADIUS + ", isTrigger=true, layer=ITEM");
		}

		return potion;
	}
}