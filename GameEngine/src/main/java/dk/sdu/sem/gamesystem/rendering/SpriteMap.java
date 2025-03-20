package dk.sdu.sem.gamesystem.rendering;

import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

public class SpriteMap {
	private final Image spriteMapImage;
	private final String name;
	private final Map<String, Sprite> sprites = new HashMap<>();

	public SpriteMap(String name, Image spriteSheetImage) {
		this.name = name;
		this.spriteMapImage = spriteSheetImage;
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
		Sprite sprite = new Sprite(name, spriteMapImage, x, y, width, height);
		sprites.put(name, sprite);
		return sprite;
	}

	/**
	 * Define sprites in a grid pattern - could be useful for uniform sprite maps
	 */
	public void defineSpritesFromGrid(int columns, int rows, double spriteWidth, double spriteHeight) {
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
}