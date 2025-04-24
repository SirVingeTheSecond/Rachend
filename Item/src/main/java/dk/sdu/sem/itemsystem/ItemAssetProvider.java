package dk.sdu.sem.itemsystem;

import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

/**
 * Asset provider for item sprites and animations.
 */
public class ItemAssetProvider implements IAssetProvider {
	private static final Logging LOGGER = Logging.createLogger("ItemAssetProvider", LoggingLevel.DEBUG);

	@Override
	public void provideAssets() {
		LOGGER.debug(ItemAssetProvider.class.getName() + ": Attempting to register item assets...");

		// Load coin sprite
		try {
			var sprite = AssetFacade.createSprite("coin")
				.withImagePath("coin_anim_f0.png")
				.load();
			LOGGER.debug("ItemAssetProvider: Successfully loaded coin sprite: " + sprite);
		} catch (Exception e) {
			LOGGER.error("ItemAssetProvider: Failed to load coin sprite");
			e.printStackTrace();
		}

		// Load potion sprite
		try {
			AssetFacade.createSprite("potion")
				.withImagePath("potion")
				.load();

			LOGGER.debug("Loaded potion sprite");
		} catch (Exception e) {
			LOGGER.error("Failed to load potion sprite: " + e.getMessage());
		}
	}
}