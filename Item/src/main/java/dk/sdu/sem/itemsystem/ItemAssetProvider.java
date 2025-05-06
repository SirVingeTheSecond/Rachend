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

		// Load healthPotion sprite
		try {
			AssetFacade.createSprite("healthPotion")
				.withImagePath("healthPotion.png")
				.load();

			LOGGER.debug("Loaded healthPotion sprite");
		} catch (Exception e) {
			LOGGER.error("Failed to load healthPotion sprite: " + e.getMessage());
		}

		// Load dmgStaff sprite
		try {
			var sprite = AssetFacade.createSprite("dmgStaff")
				.withImagePath("dmgStaff.png")
				.load();
			LOGGER.debug("ItemAssetProvider: Successfully loaded dmgStaff sprite: " + sprite);
		} catch (Exception e) {
			LOGGER.error("ItemAssetProvider: Failed to load dmgStaff sprite");
			e.printStackTrace();
		}

		// Load speedBoots sprite
		try {
			var sprite = AssetFacade.createSprite("speedBoots")
				.withImagePath("speedBoots.png")
				.load();
			LOGGER.debug("ItemAssetProvider: Successfully loaded speedBoots sprite: " + sprite);
		} catch (Exception e) {
			LOGGER.error("ItemAssetProvider: Failed to load speedBoots sprite");
			e.printStackTrace();
		}
	}
}