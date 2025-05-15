package dk.sdu.sem.levelsystem.providers;

import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

/**
 * Provider for room assets such as force fields and barriers.
 */
public class RoomAssetProvider implements IAssetProvider {
	private static final Logging LOGGER = Logging.createLogger("RoomAssetProvider", LoggingLevel.DEBUG);

	@Override
	public void provideAssets() {
		LOGGER.debug("Loading room assets");
		createForceFieldAssets();
	}

	/**
	 * Creates all force field related assets.
	 */
	private void createForceFieldAssets() {
		SpriteMap forceFieldMap = AssetFacade.createSpriteMap("force-field")
			.withImagePath("force-field-tileset.png")
			.withGrid(8, 1, 16, 16)
			.load();

		createForceFieldAnimation();

		LOGGER.debug("Force field assets loaded successfully");
	}

	/**
	 * Creates force field animation from the sprite map frames.
	 */
	private void createForceFieldAnimation() {
		AssetFacade.createAnimation("force-field-anim")
			.withSpriteMap(AssetFacade.preloadAsType("force-field", SpriteMap.class))
			.withFrameDuration(0.1f)
			.withLoop(true)
			.load();

		LOGGER.debug("Force field animation created");
	}
}