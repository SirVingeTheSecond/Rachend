package dk.sdu.sem.gamesystem.assets.registry;

/**
 * Asset registry provider for core game assets.
 */
public class CoreAssetsProvider implements IAssetRegistryProvider {
	@Override
	public void registerAssets(AssetRegistrar registrar) {
		// Register assets here

		// Images
		registrar.registerImage("floor_tileset", "floor.png");

		// Sprite maps
		registrar.registerSpriteMap("floor_tiles", "floor_tileset", 7, 7, 16, 16);

		// Individual tiles
		for (int y = 0; y < 7; y++) {
			for (int x = 0; x < 7; x++) {
				String spriteName = "tile_" + x + "_" + y;
				registrar.registerSpriteFromMap("floor_" + x + "_" + y, "floor_tiles", spriteName);
			}
		}
	}
}