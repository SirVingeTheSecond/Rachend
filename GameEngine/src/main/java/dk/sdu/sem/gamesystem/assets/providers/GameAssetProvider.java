package dk.sdu.sem.gamesystem.assets.providers;

import dk.sdu.sem.gamesystem.assets.AssetFacade;

/**
 * Asset provider for core game assets.
 */
public class GameAssetProvider implements IAssetProvider {
	@Override
	public void provideAssets() {
		// Load and slice the floor tileset
		// Parameters: name, tile width, tile height
		AssetFacade.createSpriteSheet("floor", 32, 32);

		// The system will:
		// 1. Load floor.png from the resources folder
		// 2. Auto-slice it into tiles (assuming 32x32 pixel tiles)
		// 3. Make tiles available by index
	}
}