package dk.sdu.sem.gamesystem.rendering;

import dk.sdu.sem.gamesystem.assets.IDisposable;
import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

public class SpriteMap implements IDisposable {
	private Image spriteMapImage;
	private final String name;
	private final Map<String, Sprite> sprites = new HashMap<>();
	private boolean isDisposed;

	public SpriteMap(String name, Image spriteSheetImage) {
		this.name = name;
		this.spriteMapImage = spriteSheetImage;
		this.isDisposed = false;
	}

	public String getName() {
		return name;
	}

	public Image getImage() {
		return spriteMapImage;
	}

	/**
	 * Define a sprite from this map with a specific region
	 */
	public Sprite defineSprite(String name, double x, double y, double width, double height) {
		if (isDisposed || spriteMapImage == null) {
			return null;
		}

		Sprite sprite = new Sprite(name, spriteMapImage, x, y, width, height);
		sprites.put(name, sprite);
		return sprite;
	}

	/**
	 * Define sprites in a grid pattern - could be useful for uniform sprite maps
	 */
	public void defineSpritesFromGrid(int columns, int rows, double spriteWidth, double spriteHeight) {
		if (isDisposed || spriteMapImage == null) {
			return;
		}

		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < columns; x++) {
				String spriteName = "tile_" + x + "_" + y;
				defineSprite(
					spriteName,
					x * spriteWidth,
					y * spriteHeight,
					spriteWidth,
					spriteHeight
				);
			}
		}
	}

	/**
	 * Get a sprite by name
	 */
	public Sprite getSprite(String name) {
		return sprites.get(name);
	}

	/**
	 * Get a sprite by grid coordinates
	 */
	public Sprite getSpriteByGrid(int x, int y) {
		return sprites.get("tile_" + x + "_" + y);
	}

	/**
	 * Get all sprites in this map
	 */
	public Map<String, Sprite> getAllSprites() {
		return new HashMap<>(sprites);
	}

	/**
	 * Dispose of all resources
	 */
	@Override
	public void dispose() {
		if (!isDisposed) {
			// Dispose all sprites
			for (Sprite sprite : sprites.values()) {
				sprite.dispose();
			}
			sprites.clear();

			// Allow the image to be garbage collected
			spriteMapImage = null;
			isDisposed = true;
		}
	}

	/**
	 * Check if this sprite map has been disposed
	 */
	@Override
	public boolean isDisposed() {
		return isDisposed;
	}

	/**
	 * Get the number of sprites in this map
	 */
	public int getSpriteCount() {
		return sprites.size();
	}
}