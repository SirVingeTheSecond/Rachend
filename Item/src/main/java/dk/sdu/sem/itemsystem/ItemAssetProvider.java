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
			var sprite = AssetFacade.createSprite("Coin_img")
				.withImagePath("coin_anim_f0.png")
				.load();
			LOGGER.debug("ItemAssetProvider: Successfully loaded coin sprite: " + sprite);
		} catch (Exception e) {
			LOGGER.error("ItemAssetProvider: Failed to load coin sprite");
			e.printStackTrace();
		}

		// Load smallHealthPotion sprite
		try {
			AssetFacade.createSprite("Health_Potion_img")
				.withImagePath("smallHealthPotion.png")
				.load();

			LOGGER.debug("Loaded smallHealthPotion sprite");
		} catch (Exception e) {
			LOGGER.error("Failed to load smallHealthPotion sprite: " + e.getMessage());
		}

		// Load healthPotion sprite
		try {
			AssetFacade.createSprite("Greater_Health_Potion_img")
				.withImagePath("healthPotion.png")
				.load();

			LOGGER.debug("Loaded healthPotion sprite");
		} catch (Exception e) {
			LOGGER.error("Failed to load healthPotion sprite: " + e.getMessage());
		}

		// Load energyPotion sprite
		try {
			AssetFacade.createSprite("Energy_Potion_img")
				.withImagePath("energyPotion.png")
				.load();

			LOGGER.debug("Loaded energyPotion sprite");
		} catch (Exception e) {
			LOGGER.error("Failed to load energyPotion sprite: " + e.getMessage());
		}

		// Load strengthPotion sprite
		try {
			AssetFacade.createSprite("Strength_Potion_img")
				.withImagePath("strengthPotion.png")
				.load();

			LOGGER.debug("Loaded strengthPotion sprite");
		} catch (Exception e) {
			LOGGER.error("Failed to load strengthPotion sprite: " + e.getMessage());
		}

		// Load apple sprite
		try {
			AssetFacade.createSprite("Apple_img")
				.withImagePath("apple.png")
				.load();

			LOGGER.debug("Loaded apple sprite");
		} catch (Exception e) {
			LOGGER.error("Failed to load apple sprite: " + e.getMessage());
		}

		// Load dmgStaff sprite
		try {
			var sprite = AssetFacade.createSprite("Damage_Upper_img")
				.withImagePath("dmgStaff.png")
				.load();
			LOGGER.debug("ItemAssetProvider: Successfully loaded dmgStaff sprite: " + sprite);
		} catch (Exception e) {
			LOGGER.error("ItemAssetProvider: Failed to load dmgStaff sprite");
			e.printStackTrace();
		}

		// Load speedBoots sprite
		try {
			var sprite = AssetFacade.createSprite("Speed_Upper_img")
				.withImagePath("speedBoots.png")
				.load();
			LOGGER.debug("ItemAssetProvider: Successfully loaded speedBoots sprite: " + sprite);
		} catch (Exception e) {
			LOGGER.error("ItemAssetProvider: Failed to load speedBoots sprite");
			e.printStackTrace();
		}

		// Load atkSpeedGloves sprite
		try {
			var sprite = AssetFacade.createSprite("Atk_Speed_Upper_img")
				.withImagePath("atkSpeedGloves.png")
				.load();
			LOGGER.debug("ItemAssetProvider: Successfully loaded atkSpeedGloves sprite: " + sprite);
		} catch (Exception e) {
			LOGGER.error("ItemAssetProvider: Failed to load atkSpeedGloves sprite");
			e.printStackTrace();
		}

		// Load bigBulletGuide sprite
		try {
			var sprite = AssetFacade.createSprite("Bigger_Bullets_img")
				.withImagePath("bigBulletGuide.png")
				.load();
			LOGGER.debug("ItemAssetProvider: Successfully loaded bigBulletGuide sprite: " + sprite);
		} catch (Exception e) {
			LOGGER.error("ItemAssetProvider: Failed to load bigBulletGuide sprite");
			e.printStackTrace();
		}

		// Load fastArrow sprite
		try {
			var sprite = AssetFacade.createSprite("Faster_Bullets_img")
				.withImagePath("fastArrow.png")
				.load();
			LOGGER.debug("ItemAssetProvider: Successfully loaded fastArrow sprite: " + sprite);
		} catch (Exception e) {
			LOGGER.error("ItemAssetProvider: Failed to load fastArrow sprite");
			e.printStackTrace();
		}

		// Load healthArmor sprite
		try {
			var sprite = AssetFacade.createSprite("Health_Upper_img")
				.withImagePath("healthArmor.png")
				.load();
			LOGGER.debug("ItemAssetProvider: Successfully loaded healthArmor sprite: " + sprite);
		} catch (Exception e) {
			LOGGER.error("ItemAssetProvider: Failed to load healthArmor sprite");
			e.printStackTrace();
		}

		// Load helmOfKnockback sprite
		try {
			var sprite = AssetFacade.createSprite("Knockback_Upper_img")
				.withImagePath("helmOfKnockback.png")
				.load();
			LOGGER.debug("ItemAssetProvider: Successfully loaded helmOfKnockback sprite: " + sprite);
		} catch (Exception e) {
			LOGGER.error("ItemAssetProvider: Failed to load helmOfKnockback sprite");
			e.printStackTrace();
		}

		// Load theCup sprite
		try {
			var sprite = AssetFacade.createSprite("Cup_img")
				.withImagePath("theCup.png")
				.load();
			LOGGER.debug("ItemAssetProvider: Successfully loaded theCup sprite: " + sprite);
		} catch (Exception e) {
			LOGGER.error("ItemAssetProvider: Failed to load theCup sprite");
			e.printStackTrace();
		}
	}
}