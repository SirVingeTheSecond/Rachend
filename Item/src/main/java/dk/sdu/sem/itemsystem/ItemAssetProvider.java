package dk.sdu.sem.itemsystem;

import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;

/**
 * Asset provider for item sprites and animations.
 */
public class ItemAssetProvider implements IAssetProvider {

	@Override
	public void provideAssets() {
		System.out.println(ItemAssetProvider.class.getName() + ": Attempting to register item assets...");

		// Load coin sprite
		try {
			var sprite = AssetFacade.createSprite("coin")
				.withImagePath("coin_anim_f0.png")
				.load();
			System.out.println("ItemAssetProvider: Successfully loaded coin sprite: " + sprite);
		} catch (Exception e) {
			System.err.println("ItemAssetProvider: Failed to load coin sprite");
			e.printStackTrace();
		}

		// Load potion sprite
		try {
			AssetFacade.createSprite("potion")
				.withImagePath("potion")
				.load();

			System.out.println("Loaded potion sprite");
		} catch (Exception e) {
			System.err.println("Failed to load potion sprite: " + e.getMessage());
		}
	}
}