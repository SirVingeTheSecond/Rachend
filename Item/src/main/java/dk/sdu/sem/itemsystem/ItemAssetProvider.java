package dk.sdu.sem.itemsystem;

import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;

/**
 * Asset provider for item sprites and animations.
 */
public class ItemAssetProvider implements IAssetProvider {

	@Override
	public void provideAssets() {
		System.out.println("Registering item assets...");

		// Load coin sprite
		try {
			// Try to load coin as an individual sprite
			AssetFacade.createSprite("coin")
				.withImagePath("coin_anim_f0")
				.load();

			System.out.println("Loaded coin sprite");
		} catch (Exception e) {
			System.err.println("Failed to load coin sprite: " + e.getMessage());
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