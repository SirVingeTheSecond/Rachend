package dk.sdu.sem.gamesystem.assets;

import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * All Asset management happens through this facade.
 * Provides straightforward methods to load game assets.
 */
public final class AssetFacade {
	private AssetFacade() {} // Prevent instantiation

	/**
	 * Initializes the asset system.
	 * Call this once at application startup.
	 */
	public static void initialize() {
		AssetSystem.initialize();
	}

	/**
	 * Loads a sprite by name.
	 *
	 * @param name Name of the sprite (without extension)
	 * @return The loaded sprite
	 */
	public static Sprite loadSprite(String name) {
		return AssetSystem.loadSprite(name);
	}

	/**
	 * Attempts to load a sprite, returning null if not found.
	 * Useful for convention-based loading.
	 *
	 * @param name Name of the sprite
	 * @return The sprite or null if not found
	 */
	public static Sprite tryLoadSprite(String name) {
		try {
			return loadSprite(name);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Loads an animation by name.
	 * Will auto-detect frames using naming convention if possible.
	 *
	 * @param name Base name of the animation
	 * @return The loaded animation
	 */
	public static SpriteAnimation loadAnimation(String name) {
		return AssetSystem.loadAnimation(name);
	}

	/**
	 * Loads an image by name.
	 *
	 * @param name Name of the image (without extension)
	 * @return The loaded image
	 */
	public static Image loadImage(String name) {
		return AssetSystem.loadImage(name);
	}

	/**
	 * Loads a sprite sheet by name.
	 * The sprite sheet is automatically sliced into individual tiles.
	 *
	 * @param name Name of the sprite sheet
	 * @return The loaded sprite sheet
	 */
	public static SpriteMap loadSpriteSheet(String name) {
		return AssetSystem.loadSpriteSheet(name);
	}

	/**
	 * Creates a sprite sheet and auto-slices it.
	 *
	 * @param name Name of the sprite sheet
	 * @param tileWidth Width of each tile
	 * @param tileHeight Height of each tile
	 * @return The created sprite sheet
	 */
	public static SpriteMap createSpriteSheet(String name, int tileWidth, int tileHeight) {
		Image image = loadImage(name);
		int columns = (int)(image.getWidth() / tileWidth);
		int rows = (int)(image.getHeight() / tileHeight);

		return AssetSystem.defineSpriteSheet(name, name, columns, rows, tileWidth, tileHeight);
	}

	/**
	 * Creates an animation using naming convention pattern.
	 * Looks for frames named "baseName_0", "baseName_1", etc.
	 *
	 * @param baseName Base name of the animation
	 * @param frameDuration Duration of each frame in seconds
	 * @param loop Whether the animation should loop
	 * @return The created animation
	 */
	public static SpriteAnimation createAnimation(String baseName, double frameDuration, boolean loop) {
		List<String> frames = new ArrayList<>();
		int frameIndex = 0;

		// Keep checking for frames until we don't find one
		while (true) {
			String framePath = baseName + "_" + frameIndex;
			if (!resourceExists(framePath)) break;
			frames.add(framePath);
			frameIndex++;
		}

		if (frames.isEmpty()) {
			throw new IllegalArgumentException("No frames found for animation: " + baseName);
		}

		return AssetSystem.defineAnimation(baseName, frames, frameDuration, loop);
	}

	/**
	 * Creates an animation with explicit frame names.
	 *
	 * @param name Animation name/ID
	 * @param frameNames List of exact sprite frame names
	 * @param frameDuration Duration of each frame in seconds
	 * @param loop Whether the animation should loop
	 * @return The created animation
	 */
	public static SpriteAnimation createAnimation(String name, List<String> frameNames, double frameDuration, boolean loop) {
		if (frameNames.isEmpty()) {
			throw new IllegalArgumentException("Empty frame list for animation: " + name);
		}

		return AssetSystem.defineAnimation(name, frameNames, frameDuration, loop);
	}

	/**
	 * Helper method to check if a resource exists.
	 *
	 * @param path Resource path to check
	 * @return True if resource exists, false otherwise
	 */
	private static boolean resourceExists(String path) {
		InputStream is = AssetFacade.class.getClassLoader().getResourceAsStream(path + ".png");
		if (is == null) {
			is = AssetFacade.class.getClassLoader().getResourceAsStream(path);
		}
		if (is != null) {
			try { is.close(); } catch (Exception e) {}
			return true;
		}
		return false;
	}

	/**
	 * Preloads assets to ensure they're available when needed.
	 *
	 * @param names Names of assets to preload
	 */
	public static void preload(String... names) {
		for (String name : names) {
			AssetSystem.preload(name);
		}
	}

	/**
	 * Preloads a single asset to ensure it's available when needed.
	 *
	 * @param name Name of the asset
	 */
	public static void preload(String name) {
		AssetSystem.preload(name);
	}

	/**
	 * Unloads all unused assets.
	 */
	public static void unloadUnused() {
		AssetSystem.unloadUnused();
	}
}