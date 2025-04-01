package dk.sdu.sem.gamesystem.assets;

import dk.sdu.sem.gamesystem.assets.managers.AssetManager;
import dk.sdu.sem.gamesystem.assets.references.*;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;
import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

/**
 * Domain layer for asset management.
 */
class AssetSystem {
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
		ServiceLoader.load(dk.sdu.sem.gamesystem.assets.providers.IAssetProvider.class).forEach(provider -> {
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
	 * Loads a sprite by name with detailed logging.
	 * Ensures proper namespacing to prevent type collisions.
	 */
	static Sprite loadSprite(String name) {
		return loadSprite(name, name);
	}

	/**
	 * Loads a sprite by name, with a separate image path.
	 * Ensures proper namespacing to prevent type collisions.
	 */
	static Sprite loadSprite(String name, String imagePath) {
		// Generate namespaced IDs for different asset types
		String spriteId = AssetReferenceFactory.getNamespacedAssetId(name, Sprite.class);
		String imageId = AssetReferenceFactory.getNamespacedAssetId(imagePath, Image.class);

		System.out.println("Loading sprite: " + name);
		System.out.println("Sprite ID: " + spriteId);
		System.out.println("Image ID: " + imageId);

		// Try to get from AssetManager
		AssetManager manager = AssetManager.getInstance();

		try {
			// Try to get existing sprite
			System.out.println("Checking if sprite exists: " + spriteId);
			Sprite existingSprite = manager.getAsset(new SpriteReference(spriteId));
			System.out.println("Found existing sprite: " + existingSprite.getName());
			return existingSprite;
		} catch (Exception e) {
			// Not found, need to load image and create sprite
			System.out.println("Sprite not found, loading from resources");
			try {
				// Load the image first - using the image-specific ID
				Image image;
				try {
					// First try to get an existing image
					System.out.println("Checking if image exists: " + imageId);
					image = manager.getAsset(new ImageReference(imageId));
					System.out.println("Found existing image: " + imageId);
				} catch (Exception ex) {
					// Load the image from resources
					System.out.println("Image not found, loading from resources");
					InputStream is = AssetSystem.class.getClassLoader().getResourceAsStream(imagePath + ".png");
					if (is == null) {
						is = AssetSystem.class.getClassLoader().getResourceAsStream(imagePath + ".jpg");
					}
					if (is == null) {
						is = AssetSystem.class.getClassLoader().getResourceAsStream(imagePath);
					}

					if (is == null) {
						System.err.println("Searching for image: " + imagePath);
						throw new IllegalArgumentException("Image not found: " + imagePath);
					}

					// Create image
					image = new Image(is);

					// Register with AssetManager
					AssetDescriptor<Image> descriptor = new AssetDescriptor<>(imageId, Image.class, imagePath);
					manager.registerAsset(descriptor);

					// Store the actual image
					manager.storeAsset(imageId, image);
					System.out.println("Registered image: " + imageId);
				}

				// Create sprite
				Sprite sprite = new Sprite(name, image);

				// Register with AssetManager
				AssetDescriptor<Sprite> descriptor = new AssetDescriptor<>(spriteId, Sprite.class, name);
				descriptor.setMetadata("imageId", imageId);
				manager.registerAsset(descriptor);

				// Store the actual sprite
				manager.storeAsset(spriteId, sprite);
				System.out.println("Registered sprite: " + spriteId);

				return sprite;
			} catch (Exception ex) {
				System.err.println("Failed to load sprite: " + name);
				ex.printStackTrace();
				throw new IllegalArgumentException("Failed to load sprite: " + name, ex);
			}
		}
	}

	/**
	 * Loads an image by name.
	 * Ensures proper namespacing to prevent type collisions.
	 */
	static Image loadImage(String name) {
		// Generate namespaced ID for image
		String imageId = AssetReferenceFactory.getNamespacedAssetId(name, Image.class);

		// Try to get from AssetManager
		AssetManager manager = AssetManager.getInstance();

		try {
			return manager.getAsset(new ImageReference(imageId));
		} catch (Exception e) {
			// Load the image with different extensions
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
				//Try direct file
				try {
					is = new FileInputStream(name);
				} catch (FileNotFoundException ex) {
					throw new IllegalArgumentException("Image not found: " + name);
				}
			}

			if (is == null) {
				throw new IllegalArgumentException("Image not found: " + name);
			}

			// Create image
			Image image = new Image(is);

			// Register with AssetManager
			AssetDescriptor<Image> descriptor = new AssetDescriptor<>(imageId, Image.class, name);
			manager.registerAsset(descriptor);

			// Store the actual image
			manager.storeAsset(imageId, image);

			return image;
		}
	}

	/**
	 * Loads or creates a sprite sheet.
	 * Ensures proper namespacing to prevent type collisions.
	 */
	static SpriteMap loadSpriteSheet(String name) {
		// Generate namespaced ID for sprite sheet
		String sheetId = AssetReferenceFactory.getNamespacedAssetId(name, SpriteMap.class);

		// Try to get from AssetManager
		AssetManager manager = AssetManager.getInstance();

		try {
			return manager.getAsset(new SpriteMapReference(sheetId));
		} catch (Exception e) {
			// Create a new sheet with auto-detected tile size
			Image image = loadImage(name);

			// Auto-detect tile size
			int tileSize = detectTileSize(image);
			int columns = (int)(image.getWidth() / tileSize);
			int rows = (int)(image.getHeight() / tileSize);

			return defineSpriteSheet(name, name, columns, rows, tileSize, tileSize);
		}
	}

	/**
	 * Create a sprite sheet with the given parameters.
	 * Ensures proper namespacing to prevent type collisions.
	 */
	static SpriteMap defineSpriteSheet(String name, String imageName, int columns, int rows,
									   double tileWidth, double tileHeight) {
		// Generate namespaced ID for sprite sheet
		String sheetId = AssetReferenceFactory.getNamespacedAssetId(name, SpriteMap.class);
		String imageId = AssetReferenceFactory.getNamespacedAssetId(imageName, Image.class);

		// Load the image
		Image image = loadImage(imageName);

		// Create sprite sheet
		SpriteMap sheet = new SpriteMap(name, image, columns, rows, tileWidth, tileHeight);

		// Register with AssetManager
		AssetManager manager = AssetManager.getInstance();
		AssetDescriptor<SpriteMap> descriptor = new AssetDescriptor<>(sheetId, SpriteMap.class, name);
		descriptor.setMetadata("imageId", imageId);
		descriptor.setMetadata("columns", columns);
		descriptor.setMetadata("rows", rows);
		descriptor.setMetadata("spriteWidth", tileWidth);
		descriptor.setMetadata("spriteHeight", tileHeight);
		manager.registerAsset(descriptor);

		// Store the actual sprite map
		manager.storeAsset(sheetId, sheet);

		return sheet;
	}

	/**
	 * Creates an animation from a sprite map using all tiles in sequential order.
	 * Uses references to tiles within the sprite map.
	 */
	static SpriteAnimation defineAnimationFromSpriteMap(String name, SpriteMap spriteMap,
														double frameDuration, boolean loop) {
		if (spriteMap == null) {
			throw new IllegalArgumentException("SpriteMap cannot be null");
		}

		// Generate namespaced ID for animation
		String animId = AssetReferenceFactory.getNamespacedAssetId(name, SpriteAnimation.class);

		// Get all tiles from the sprite map
		Map<Integer, Sprite> tiles = spriteMap.getAllTiles();
		if (tiles.isEmpty()) {
			throw new IllegalArgumentException("SpriteMap has no tiles: " + spriteMap.getName());
		}

		// Create tile references for all frames
		List<IAssetReference<Sprite>> frameReferences = new ArrayList<>();
		int maxIndex = tiles.keySet().stream().max(Integer::compareTo).orElse(-1);
		for (int i = 0; i <= maxIndex; i++) {
			if (tiles.containsKey(i)) {
				// Create a reference to this tile in the sprite map
				frameReferences.add(AssetReferenceFactory.createSpriteMapTileReference(
					spriteMap.getName(), i));
			}
		}

		if (frameReferences.isEmpty()) {
			throw new IllegalArgumentException("No valid frames found in sprite map: " + spriteMap.getName());
		}

		// Create animation with the references
		SpriteAnimation animation = new SpriteAnimation(frameReferences, frameDuration, loop);

		// Register with AssetManager
		AssetManager manager = AssetManager.getInstance();
		AssetDescriptor<SpriteAnimation> descriptor = new AssetDescriptor<>(animId, SpriteAnimation.class, name);
		descriptor.setMetadata("spriteMapId", spriteMap.getName());
		descriptor.setMetadata("frameDuration", frameDuration);
		descriptor.setMetadata("looping", loop);
		manager.registerAsset(descriptor);

		// Store the actual animation
		manager.storeAsset(animId, animation);

		return animation;
	}

	/**
	 * Define an animation from a sprite map using specific tile indices.
	 * Uses references to tiles within the sprite map.
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

		// Generate namespaced ID for animation
		String animId = AssetReferenceFactory.getNamespacedAssetId(name, SpriteAnimation.class);

		// Convert indices to sprite map tile references
		List<IAssetReference<Sprite>> frameReferences = new ArrayList<>();
		for (int index : tileIndices) {
			// First verify the tile exists in the sprite map
			Sprite sprite = spriteMap.getTile(index);
			if (sprite == null) {
				throw new IllegalArgumentException("Tile index " + index + " not found in sprite map: " + spriteMap.getName());
			}

			// Create a reference to this tile in the sprite map
			SpriteMapTileReference tileRef = AssetReferenceFactory.createSpriteMapTileReference(
				spriteMap.getName(), index);
			frameReferences.add(tileRef);
		}

		// Create animation from tile references
		SpriteAnimation animation = new SpriteAnimation(frameReferences, frameDuration, loop);

		// Register with AssetManager
		AssetManager manager = AssetManager.getInstance();
		AssetDescriptor<SpriteAnimation> descriptor = new AssetDescriptor<>(animId, SpriteAnimation.class, name);
		descriptor.setMetadata("spriteMapId", spriteMap.getName());
		descriptor.setMetadata("tileIndices", tileIndices);
		descriptor.setMetadata("frameDuration", frameDuration);
		descriptor.setMetadata("looping", loop);
		manager.registerAsset(descriptor);

		// Store the actual animation
		manager.storeAsset(animId, animation);

		return animation;
	}

	/**
	 * Creates an animation from a sprite map using a range of tile indices.
	 */
	static SpriteAnimation defineAnimationFromSpriteMap(String name, SpriteMap spriteMap,
														int startIndex, int endIndex,
														double frameDuration, boolean loop) {
		if (startIndex > endIndex) {
			throw new IllegalArgumentException("Start index must be less than or equal to end index");
		}

		// Create array of indices
		int[] tileIndices = new int[(endIndex - startIndex) + 1];
		for (int i = 0; i < tileIndices.length; i++) {
			tileIndices[i] = startIndex + i;
		}

		return defineAnimationFromSpriteMap(name, spriteMap, tileIndices, frameDuration, loop);
	}

	/**
	 * Creates an animation with automatic preloading of sprites.
	 * This method ensures all sprites are loaded before creating the animation.
	 *
	 * @param name Animation name
	 * @param frameNames List of sprite frame names
	 * @param frameDuration Duration per frame in seconds
	 * @param loop Whether the animation should loop
	 * @return The created animation
	 */
	static SpriteAnimation createAnimationWithPreloading(String name, List<String> frameNames,
														 double frameDuration, boolean loop) {
		System.out.println("Creating animation with preloading: " + name);

		// Create properly namespaced sprite references
		List<IAssetReference<Sprite>> frameReferences = new ArrayList<>();

		for (String frameName : frameNames) {
			try {
				// Ensure the sprite is loaded first
				Sprite sprite = loadSprite(frameName);

				// Important: Use the properly namespaced ID for referencing
				String namespacedId = AssetReferenceFactory.getNamespacedAssetId(frameName, Sprite.class);
				System.out.println("Creating reference to namespaced ID: " + namespacedId);

				// Create a reference using the namespaced ID
				frameReferences.add(new SpriteReference(namespacedId));
			} catch (Exception e) {
				System.err.println("Error creating reference for frame: " + frameName);
				e.printStackTrace();
			}
		}

		if (frameReferences.isEmpty()) {
			throw new IllegalArgumentException("Failed to load any sprites for animation: " + name);
		}

		// Generate namespaced ID for animation
		String animId = AssetReferenceFactory.getNamespacedAssetId(name, SpriteAnimation.class);

		// Create animation with the properly namespaced references
		SpriteAnimation animation = new SpriteAnimation(frameReferences, frameDuration, loop);

		// Register with AssetManager
		AssetManager manager = AssetManager.getInstance();
		AssetDescriptor<SpriteAnimation> descriptor = new AssetDescriptor<>(
			animId, SpriteAnimation.class, name);
		descriptor.setMetadata("frameDuration", frameDuration);
		descriptor.setMetadata("looping", loop);
		manager.registerAsset(descriptor);

		// Store the actual animation
		manager.storeAsset(animId, animation);

		return animation;
	}

	/**
	 * Auto-detect tile size from image.
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
	 * Prioritizes loading as a sprite sheet first to handle common naming conflicts.
	 */
	static void preload(String name) {
		// Try all asset types
		try {
			preloadAsType(name, SpriteMap.class);
			return; // Success, no need to try other types
		} catch (Exception e) {
			// Not a sprite sheet, continue
		}

		try {
			preloadAsType(name, SpriteAnimation.class);
			return; // Success, no need to try other types
		} catch (Exception e) {
			// Not an animation, continue
		}

		try {
			preloadAsType(name, Sprite.class);
			return; // Success, no need to try other types
		} catch (Exception e) {
			// Not a sprite, try as image
			try {
				preloadAsType(name, Image.class);
			} catch (Exception e2) {
				// Couldn't preload - might not exist yet
				System.err.println("Warning: Could not preload asset: " + name);
			}
		}
	}

	/**
	 * Preloads an asset of a specific type.
	 */
	@SuppressWarnings("unchecked")
	static <T> T preloadAsType(String name, Class<T> assetType) {
		if (assetType == Sprite.class) {
			return (T) loadSprite(name);
		} else if (assetType == SpriteMap.class) {
			return (T) loadSpriteSheet(name);
		} else if (assetType == SpriteAnimation.class) {
			throw new IllegalArgumentException("Cannot preload animations directly. Use specific loading methods.");
		} else if (assetType == Image.class) {
			return (T) loadImage(name);
		} else {
			throw new IllegalArgumentException("Unsupported asset type: " + assetType.getName());
		}
	}

	/**
	 * Unloads all unused assets.
	 */
	static void unloadUnused() {
		AssetManager.getInstance().unloadUnusedAssets();
	}
}