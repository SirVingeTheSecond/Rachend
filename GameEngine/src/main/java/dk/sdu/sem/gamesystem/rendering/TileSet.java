package dk.sdu.sem.gamesystem.rendering;

import java.util.HashMap;
import java.util.Map;

public class TileSet {
	private final SpriteMap spriteSheet;
	private final Map<Integer, Sprite> tiles = new HashMap<>();
	private final String name;

	public TileSet(String name, SpriteMap spriteSheet) {
		this.name = name;
		this.spriteSheet = spriteSheet;
	}

	public String getName() {
		return name;
	}

	public SpriteMap getSpriteSheet() {
		return spriteSheet;
	}

	/**
	 * Define a tile with a specific ID linking to a sprite
	 */
	public void defineTile(int id, String spriteName) {
		Sprite sprite = spriteSheet.getSprite(spriteName);
		if (sprite != null) {
			tiles.put(id, sprite);
		}
	}

	/**
	 * Define a tile with a specific ID linking to grid coordinates
	 */
	public void defineTileByGrid(int id, int x, int y) {
		defineTile(id, "tile_" + x + "_" + y);
	}

	/**
	 * Automatically define all tiles in a grid pattern with sequential IDs
	 */
	public void defineAllTilesFromGrid(int columns, int rows) {
		int id = 0;
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < columns; x++) {
				defineTileByGrid(id++, x, y);
			}
		}
	}

	/**
	 * Get a tile sprite by its ID
	 */
	public Sprite getTileSprite(int id) {
		return tiles.get(id);
	}
}