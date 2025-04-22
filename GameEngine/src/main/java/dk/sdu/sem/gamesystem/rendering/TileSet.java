package dk.sdu.sem.gamesystem.rendering;

import dk.sdu.sem.gamesystem.assets.IDisposable;

import java.util.HashMap;
import java.util.Map;

// Does this even have a use-case?
public class TileSet implements IDisposable {
	private final SpriteMap spriteSheet;
	private final Map<Integer, Sprite> tiles = new HashMap<>();
	private final String name;
	private boolean isDisposed;

	public TileSet(String name, SpriteMap spriteSheet) {
		this.name = name;
		this.spriteSheet = spriteSheet;
		this.isDisposed = false;
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
		if (isDisposed) return;

		Sprite sprite = spriteSheet.getSprite(spriteName);
		if (sprite != null) {
			tiles.put(id, sprite);
		}
	}

	/**
	 * Define a tile with a specific ID linking to grid coordinates
	 */
	public void defineTileByGrid(int id, int x, int y) {
		if (!isDisposed) {
			defineTile(id, "tile_" + x + "_" + y);
		}
	}

	/**
	 * Automatically define all tiles in a grid pattern with sequential IDs
	 */
	public void defineAllTilesFromGrid(int columns, int rows) {
		if (isDisposed) return;

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
		if (isDisposed) return null;
		return tiles.get(id);
	}

	/**
	 * Clean up all resources
	 */
	@Override
	public void dispose() {
		if (!isDisposed) {
			// Clear references to sprites
			// We don't dispose the sprites as they are owned by the spriteSheet
			tiles.clear();
			isDisposed = true;
		}
	}

	/**
	 * Check if this tileset has been disposed
	 */
	@Override
	public boolean isDisposed() {
		return isDisposed;
	}
}