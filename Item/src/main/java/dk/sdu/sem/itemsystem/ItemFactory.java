package dk.sdu.sem.itemsystem;

import dk.sdu.sem.collision.PhysicsLayer;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.commonitem.IItemFactory;
import dk.sdu.sem.commonitem.ItemComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.factories.IEntityFactory;
import dk.sdu.sem.gamesystem.rendering.Sprite;

/**
 * Factory for creating item entities.
 */
public class ItemFactory implements IItemFactory {

	@Override
	public Entity create() {
		// Default implementation creates a coin at center of screen
		return createCoin(new Vector2D(400, 300));
	}

	/**
	 * Creates a coin item at the specified position.
	 */
	public Entity createCoin(Vector2D position) {
		Entity coin = new Entity();

		// Add transform with slightly larger scale for visibility
		coin.addComponent(new TransformComponent(position, 0, new Vector2D(1.5f, 1.5f)));

		IAssetReference<Sprite> spriteRef;
		spriteRef = AssetFacade.createSpriteReference("coin");

		// Add sprite renderer component
		SpriteRendererComponent spriteRenderer = new SpriteRendererComponent(spriteRef);
		spriteRenderer.setRenderLayer(GameConstants.LAYER_OBJECTS);
		coin.addComponent(spriteRenderer);

		// Add trigger collider
		ColliderComponent collider = new ColliderComponent(
			coin,
			new Vector2D(0,0),
			6.0f,
			true,
			PhysicsLayer.ITEM
		);

		coin.addComponent(collider);

		// Add item component
		coin.addComponent(new ItemComponent("coin", 1));

		return coin;
	}

	/**
	 * Creates a health potion item at the specified position.
	 */
	public Entity createHealthPotion(Vector2D position) {
		Entity potion = new Entity();

		// Add transform component
		potion.addComponent(new TransformComponent(position, 0, new Vector2D(1.5f, 1.5f)));

		IAssetReference<Sprite> spriteRef;
		spriteRef = AssetFacade.createSpriteReference("potion");

		// Add sprite renderer component
		SpriteRendererComponent spriteRenderer = new SpriteRendererComponent(spriteRef);
		spriteRenderer.setRenderLayer(GameConstants.LAYER_OBJECTS);
		potion.addComponent(spriteRenderer);

		// Add collider as trigger
		ColliderComponent collider = new ColliderComponent(
			potion,
			new Vector2D(0,0),
			6.0f,
			true,
			PhysicsLayer.ITEM
		);

		potion.addComponent(collider);

		// Add item component
		potion.addComponent(new ItemComponent("health_potion", 1));

		return potion;
	}
}