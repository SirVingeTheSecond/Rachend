package dk.sdu.sem.gamesystem.assets.registry;

import java.util.Arrays;

public class GameAssetsProvider implements IAssetRegistryProvider {
	@Override
	public void registerAssets(AssetRegistrar registrar) {
		// Register floor tileset
		registrar.registerImage("floor_tileset_image", "floor.png");

		// Create a sprite map for the tileset (7x7 grid of 16x16 tiles)
		registrar.registerSpriteMap(
			"floor_tiles",
			"floor_tileset_image",
			7, 7,   // columns, rows
			16, 16  // tile width, tile height
		);

		// Register individual tiles from the sprite map
		for (int y = 0; y < 7; y++) {
			for (int x = 0; x < 7; x++) {
				registrar.registerSpriteFromMap(
					"floor_" + x + "_" + y,
					"floor_tiles",
					"tile_" + x + "_" + y
				);
			}
		}
	}
}