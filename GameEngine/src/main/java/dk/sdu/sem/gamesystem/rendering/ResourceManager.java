package dk.sdu.sem.gamesystem.rendering;

import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

public class ResourceManager {
	private static final ResourceManager instance = new ResourceManager();

	private final Map<String, Image> images = new HashMap<>();
	private final Map<String, SpriteMap> spriteSheets = new HashMap<>();
	private final Map<String, TileSet> tileSets = new HashMap<>();

	public static ResourceManager getInstance() {
		return instance;
	}

	/**
	 * Load an image from a path
	 */
	public Image loadImage(String path) {
		if (!images.containsKey(path)) {
			try {
				// Try to load the image directly
				var inputStream = getClass().getClassLoader().getResourceAsStream(path);

				if (inputStream == null) {
					System.err.println("Resource not found: " + path);
					System.err.println("Available resources in classpath:");
					try {
						var resources = getClass().getClassLoader().getResources("");
						while (resources.hasMoreElements()) {
							System.err.println(" - " + resources.nextElement());
						}
					} catch (Exception e) {
						System.err.println("Failed to list resources: " + e.getMessage());
					}
					return null;
				}

				Image image = new Image(inputStream);
				images.put(path, image);
				System.out.println("Successfully loaded image: " + path);
			} catch (Exception e) {
				System.err.println("Failed to load image: " + path);
				e.printStackTrace();
			}
		}
		return images.get(path);
	}

	/**
	 * Create a sprite sheet from an image
	 */
	public SpriteMap createSpriteSheet(String name, String imagePath) {
		if (spriteSheets.containsKey(name)) {
			return spriteSheets.get(name);
		}

		Image image = loadImage(imagePath);
		if (image != null) {
			SpriteMap spriteSheet = new SpriteMap(name, image);
			spriteSheets.put(name, spriteSheet);
			System.out.println("Created sprite sheet: " + name + " from " + imagePath);
			return spriteSheet;
		}
		System.err.println("Failed to create sprite sheet: " + name + " from " + imagePath);
		return null;
	}

	/**
	 * Get a sprite sheet by name
	 */
	public SpriteMap getSpriteSheet(String name) {
		return spriteSheets.get(name);
	}

	/**
	 * Create a tileset from a sprite sheet
	 */
	public TileSet createTileSet(String name, String spriteSheetName) {
		if (tileSets.containsKey(name)) {
			return tileSets.get(name);
		}

		SpriteMap spriteSheet = spriteSheets.get(spriteSheetName);
		if (spriteSheet != null) {
			TileSet tileSet = new TileSet(name, spriteSheet);
			tileSets.put(name, tileSet);
			System.out.println("Created tile set: " + name + " from " + spriteSheetName);
			return tileSet;
		}
		System.err.println("Failed to create tile set: " + name + " from " + spriteSheetName);
		return null;
	}

	/**
	 * Get a tileset by name
	 */
	public TileSet getTileSet(String name) {
		return tileSets.get(name);
	}

	/**
	 * Clear all loaded resources
	 */
	public void clearAll() {
		images.clear();
		spriteSheets.clear();
		tileSets.clear();
	}
}