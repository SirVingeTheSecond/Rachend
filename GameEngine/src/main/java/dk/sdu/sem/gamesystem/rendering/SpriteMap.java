package dk.sdu.sem.gamesystem.rendering;

import dk.sdu.sem.gamesystem.assets.IDisposable;
import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a sprite sheet with multiple tiles/sprites.
 */
public class SpriteMap implements IDisposable {
	private Image spriteMapImage;
	private final String name;
	private final Map<Integer, Sprite> tiles = new HashMap<>();
	private final Map<String, Sprite> namedSprites = new HashMap<>();
	private boolean isDisposed;
	private int columns;
	private int rows;
	private double tileWidth;
	private double tileHeight;

	/**
	 * Creates a sprite map with a sprite sheet image.
	 */
	public SpriteMap(String name, Image spriteSheetImage) {
		this.name = name;
		this.spriteMapImage = spriteSheetImage;
		this.isDisposed = false;
	}

	/**
	 * Creates a sprite map with a sprite sheet image and grid dimensions.
	 */
	public SpriteMap(String name, Image image, int columns, int rows, double tileWidth, double tileHeight) {
		this.name = name;
		this.spriteMapImage = image;
		this.columns = columns;
		this.rows = rows;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.isDisposed = false;

		// Auto-create the grid
		defineSpritesFromGrid(columns, rows, tileWidth, tileHeight);
	}

	/**
	 * Gets the name of this sprite map.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the sprite map image.
	 */
	public Image getImage() {
		return spriteMapImage;
	}

	/**
	 * Gets the number of columns.
	 */
	public int getColumns() {
		return columns;
	}

	/**
	 * Gets the number of rows.
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * Gets the width of each tile.
	 */
	public double getTileWidth() {
		return tileWidth;
	}

	/**
	 * Gets the height of each tile.
	 */
	public double getTileHeight() {
		return tileHeight;
	}

	/**
	 * Define a sprite from this map with a specific region.
	 */
	public Sprite defineSprite(String name, double x, double y, double width, double height) {
		if (isDisposed || spriteMapImage == null) {
			return null;
		}

		Sprite sprite = new Sprite(name, spriteMapImage, x, y, width, height);
		namedSprites.put(name, sprite);
		return sprite;
	}

	/**
	 * Define sprites in a grid pattern.
	 */
	public void defineSpritesFromGrid(int columns, int rows, double spriteWidth, double spriteHeight) {
		if (isDisposed || spriteMapImage == null) {
			return;
		}

		this.columns = columns;
		this.rows = rows;
		this.tileWidth = spriteWidth;
		this.tileHeight = spriteHeight;

		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < columns; x++) {
				int index = y * columns + x;
				String spriteName = "tile_" + x + "_" + y;

				Sprite sprite = defineSprite(
					spriteName,
					x * spriteWidth,
					y * spriteHeight,
					spriteWidth,
					spriteHeight
				);

				tiles.put(index, sprite);
			}
		}
	}

	/**
	 * Get a sprite by name.
	 */
	public Sprite getSprite(String name) {
		return namedSprites.get(name);
	}

	/**
	 * Get a tile by index.
	 */
	public Sprite getTile(int index) {
		return tiles.get(index);
	}

	/**
	 * Get a tile by grid coordinates.
	 */
	public Sprite getTileAt(int x, int y) {
		if (x >= 0 && x < columns && y >= 0 && y < rows) {
			return tiles.get(y * columns + x);
		}
		return null;
	}

	/**
	 * Get all sprites in this map.
	 */
	public Map<String, Sprite> getAllSprites() {
		return new HashMap<>(namedSprites);
	}

	/**
	 * Get all tiles by index.
	 */
	public Map<Integer, Sprite> getAllTiles() {
		return new HashMap<>(tiles);
	}

	/**
	 * Dispose of all resources.
	 */
	@Override
	public void dispose() {
		if (!isDisposed) {
			// Dispose all sprites
			for (Sprite sprite : namedSprites.values()) {
				sprite.dispose();
			}
			namedSprites.clear();
			tiles.clear();

			// Allow the image to be garbage collected
			spriteMapImage = null;
			isDisposed = true;
		}
	}

	/**
	 * Check if this sprite map has been disposed.
	 */
	@Override
	public boolean isDisposed() {
		return isDisposed;
	}

	/**
	 * Get the number of sprites in this map.
	 */
	public int getSpriteCount() {
		return namedSprites.size();
	}
}