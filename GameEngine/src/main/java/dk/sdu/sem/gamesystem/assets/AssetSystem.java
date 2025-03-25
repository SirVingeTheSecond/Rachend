package dk.sdu.sem.gamesystem.assets;

import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Internal implementation of the asset system.
 */
class AssetSystem {
	// Cache of loaded assets
	private static final Map<String, Object> assetCache = new ConcurrentHashMap<>();

	// Reference counts for loaded assets
	private static final Map<String, Integer> refCounts = new ConcurrentHashMap<>();

	// Track animations we've defined
	private static final Map<String, SpriteAnimation> animations = new ConcurrentHashMap<>();

	// Track sprite sheets we've defined
	private static final Map<String, SpriteMap> spriteSheets = new ConcurrentHashMap<>();

	/**
	 * Initializes the asset system.
	 */
	static void initialize() {
		// Load asset providers
		loadAssetProviders();
	}

	/**
	 * Loads all asset providers.
	 */
	private static void loadAssetProviders() {
		ServiceLoader.load(IAssetProvider.class).forEach(provider -> {
			try {
				System.out.println("Loading assets from: " + provider.getClass().getName());
				provider.provideAssets();
			} catch (Exception e) {
				System.err.println("Error loading assets from provider: " + provider.getClass().getName());
				e.printStackTrace();
			}
		});
	}

	/**
	 * Loads a sprite by name.
	 */
	static Sprite loadSprite(String name) {
		// Check cache first
		Sprite sprite = getCachedAsset(name, Sprite.class);
		if (sprite != null) {
			return sprite;
		}

		// Try to load the image
		Image image = loadImage(name);

		// Create sprite
		sprite = new Sprite(name, image);

		// Cache it
		cacheAsset(name, sprite);

		return sprite;
	}

	/**
	 * Loads an animation by name.
	 */
	static SpriteAnimation loadAnimation(String name) {
		// Check if we've already defined this animation
		SpriteAnimation animation = animations.get(name);
		if (animation != null) {
			return animation;
		}

		// Check cache
		animation = getCachedAsset(name, SpriteAnimation.class);
		if (animation != null) {
			return animation;
		}

		// Try to auto-detect frames
		List<String> frameNames = new ArrayList<>();
		int frameIndex = 0;

		while (true) {
			String frameName = name + "_" + frameIndex;
			try {
				// Check if this frame exists
				loadSprite(frameName);
				frameNames.add(frameName);
				frameIndex++;
			} catch (Exception e) {
				// No more frames
				break;
			}
		}

		// If we found frames, create the animation
		if (!frameNames.isEmpty()) {
			return defineAnimation(name, frameNames, 0.1, true);
		}

		throw new IllegalArgumentException("Animation not found and could not be auto-detected: " + name);
	}

	/**
	 * Loads an image by name.
	 */
	static Image loadImage(String name) {
		// Check cache first
		Image image = getCachedAsset(name, Image.class);
		if (image != null) {
			return image;
		}

		// Try to load the image with different extensions
		InputStream is = null;

		// Try with png extension
		is = AssetSystem.class.getClassLoader().getResourceAsStream(name + ".png");
		if (is == null) {
			// Try with jpg extension
			is = AssetSystem.class.getClassLoader().getResourceAsStream(name + ".jpg");
		}
		if (is == null) {
			// Try exact name
			is = AssetSystem.class.getClassLoader().getResourceAsStream(name);
		}

		if (is == null) {
			throw new IllegalArgumentException("Image not found: " + name);
		}

		// Create image
		image = new Image(is);

		// Cache it
		cacheAsset(name, image);

		return image;
	}

	/**
	 * Loads or creates a sprite sheet.
	 */
	static SpriteMap loadSpriteSheet(String name) {
		// Check if we've defined this sheet
		SpriteMap sheet = spriteSheets.get(name);
		if (sheet != null) {
			return sheet;
		}

		// Check cache
		sheet = getCachedAsset(name, SpriteMap.class);
		if (sheet != null) {
			return sheet;
		}

		// Try to create a new sheet with auto-detected tile size
		Image image = loadImage(name);

		// Auto-detect tile size (assume square tiles for simplicity)
		// In a real implementation, you'd have more sophisticated detection
		int tileSize = detectTileSize(image);
		int columns = (int)(image.getWidth() / tileSize);
		int rows = (int)(image.getHeight() / tileSize);

		return defineSpriteSheet(name, name, columns, rows, tileSize, tileSize);
	}

	/**
	 * Create a sprite sheet with the given parameters.
	 */
	static SpriteMap defineSpriteSheet(String name, String imageName, int columns, int rows,
									   double tileWidth, double tileHeight) {
		// Load the image
		Image image = loadImage(imageName);

		// Create sprite sheet
		SpriteMap sheet = new SpriteMap(name, image, columns, rows, tileWidth, tileHeight);

		// Cache it
		cacheAsset(name, sheet);
		spriteSheets.put(name, sheet);

		return sheet;
	}

	/**
	 * Define an animation with the given frames.
	 */
	static SpriteAnimation defineAnimation(String name, List<String> frameNames,
										   double frameDuration, boolean loop) {
		// Load all frames
		List<Sprite> frames = new ArrayList<>();
		for (String frameName : frameNames) {
			frames.add(loadSprite(frameName));
		}

		// Create animation
		SpriteAnimation animation = new SpriteAnimation(frames, frameDuration, loop);

		// Cache it
		cacheAsset(name, animation);
		animations.put(name, animation);

		return animation;
	}

	/**
	 * Define an animation from a sprite map using all tiles in sequential order.
	 */
	static SpriteAnimation defineAnimationFromSpriteMap(String name, SpriteMap spriteMap,
														double frameDuration, boolean loop) {
		if (spriteMap == null) {
			throw new IllegalArgumentException("SpriteMap cannot be null");
		}

		// Get all tiles from the sprite map
		Map<Integer, Sprite> tiles = spriteMap.getAllTiles();
		if (tiles.isEmpty()) {
			throw new IllegalArgumentException("SpriteMap has no tiles: " + spriteMap.getName());
		}

		// Convert tiles to list of sprites in order
		List<Sprite> frames = new ArrayList<>();
		int maxIndex = tiles.keySet().stream().max(Integer::compareTo).orElse(-1);
		for (int i = 0; i <= maxIndex; i++) {
			Sprite sprite = tiles.get(i);
			if (sprite != null) {
				frames.add(sprite);
			}
		}

		if (frames.isEmpty()) {
			throw new IllegalArgumentException("No valid frames found in sprite map: " + spriteMap.getName());
		}

		// Create animation directly from sprites
		SpriteAnimation animation = new SpriteAnimation(frames, frameDuration, loop);

		// Cache it
		cacheAsset(name, animation);
		animations.put(name, animation);

		return animation;
	}

	/**
	 * Define an animation from a sprite map using specific tile indices.
	 */
	static SpriteAnimation defineAnimationFromSpriteMap(String name, SpriteMap spriteMap,
														int[] tileIndices,
														double frameDuration, boolean loop) {
		if (spriteMap == null) {
			throw new IllegalArgumentException("SpriteMap cannot be null");
		}
		if (tileIndices == null || tileIndices.length == 0) {
			throw new IllegalArgumentException("Tile indices array cannot be empty");
		}

		// Convert indices to list of sprites
		List<Sprite> frames = new ArrayList<>();
		for (int index : tileIndices) {
			Sprite sprite = spriteMap.getTile(index);
			if (sprite == null) {
				throw new IllegalArgumentException("Tile index " + index + " not found in sprite map: " + spriteMap.getName());
			}
			frames.add(sprite);
		}

		// Create animation directly from sprites
		SpriteAnimation animation = new SpriteAnimation(frames, frameDuration, loop);

		// Cache it
		cacheAsset(name, animation);
		animations.put(name, animation);

		return animation;
	}

	/**
	 * Auto-detect tile size from image.
	 * This is a simple implementation - in a real game you might use
	 * more sophisticated detection algorithms.
	 */
	private static int detectTileSize(Image image) {
		// Common tile sizes to check
		int[] commonSizes = {8, 16, 32, 64, 128};

		// Find the largest common size that divides evenly
		for (int i = commonSizes.length - 1; i >= 0; i--) {
			int size = commonSizes[i];
			if (image.getWidth() % size == 0 && image.getHeight() % size == 0) {
				return size;
			}
		}

		// Default to 32 if we can't detect
		return 32;
	}

	/**
	 * Preloads an asset.
	 */
	static void preload(String name) {
		try {
			// Try loading as different asset types
			loadSprite(name);
		} catch (Exception e) {
			// Not a sprite, try as animation
			try {
				loadAnimation(name);
			} catch (Exception e2) {
				// Not an animation, try as sprite sheet
				try {
					loadSpriteSheet(name);
				} catch (Exception e3) {
					// Couldn't preload - might not exist yet
					System.err.println("Warning: Could not preload asset: " + name);
				}
			}
		}
	}

	/**
	 * Unloads all unused assets.
	 */
	static void unloadUnused() {
		// Remove assets with ref count 0
		List<String> toRemove = new ArrayList<>();

		for (Map.Entry<String, Integer> entry : refCounts.entrySet()) {
			if (entry.getValue() <= 0) {
				toRemove.add(entry.getKey());
			}
		}

		for (String key : toRemove) {
			Object asset = assetCache.remove(key);
			refCounts.remove(key);

			// Dispose if it's disposable
			if (asset instanceof IDisposable) {
				((IDisposable)asset).dispose();
			}
		}
	}

	/**
	 * Gets an asset from cache.
	 */
	@SuppressWarnings("unchecked")
	private static <T> T getCachedAsset(String name, Class<T> type) {
		Object asset = assetCache.get(name);
		if (asset != null && type.isInstance(asset)) {
			// Increment ref count
			refCounts.compute(name, (k, v) -> v == null ? 1 : v + 1);
			return (T)asset;
		}
		return null;
	}

	/**
	 * Caches an asset.
	 */
	private static <T> void cacheAsset(String name, T asset) {
		assetCache.put(name, asset);
		refCounts.put(name, 1);
	}
}