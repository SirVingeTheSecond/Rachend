package dk.sdu.sem.playersystem;

import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;

/**
 * Asset provider for player-related assets.
 * Uses a pure reference-based approach for sprite animations.
 */
public class PlayerAssetProvider implements IAssetProvider {
	@Override
	public void provideAssets() {
		SpriteMap playerSpriteMap = AssetFacade.createSpriteSheet(
			"Test",     // Image name
			17, 17      // Tile width and height
		);

		AssetFacade.createAnimationFromSpriteMap(
			"player_idle",    // Animation name
			playerSpriteMap,  // The sprite map
			0.1,              // 0.1 seconds per frame = 10 FPS
			true              // Loop the animation
		);

		AssetFacade.createAnimationFromSpriteMap(
			"player_run",    // Animation name
			playerSpriteMap,  // The sprite map
			0.1,              // 0.1 seconds per frame = 10 FPS
			true              // Loop the animation
		);
	}
}