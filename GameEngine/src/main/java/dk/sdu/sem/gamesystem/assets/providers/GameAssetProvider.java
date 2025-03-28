package dk.sdu.sem.gamesystem.assets.providers;

import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;

/**
 * Asset provider for core game assets.
 */
public class GameAssetProvider implements IAssetProvider {
	@Override
	public void provideAssets() {
		SpriteMap floorTileset = AssetFacade.createSpriteMap("floor")
			.withGrid(7, 7, 32, 32)
			.load();
	}
}