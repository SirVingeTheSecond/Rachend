package dk.sdu.sem.gamesystem.assets;

import dk.sdu.sem.gamesystem.assets.managers.AssetManager;
import dk.sdu.sem.gamesystem.assets.references.AssetReferenceFactory;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.assets.references.SpriteMapTileReference;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;
import javafx.scene.image.Image;

import java.util.Arrays;
import java.util.List;

/**
 * API for asset management.
 * This facade provides a simple interface to the asset system.
 */
public final class AssetFacade {
	private AssetFacade() {}

	/**
	 * Initializes the asset system.
	 * Call this once at application startup.
	 */
	public static void initialize() {
		AssetSystem.initialize();
	}

	/**
	 * Creates a new sprite builder.
	 * @param name Base name of the sprite
	 * @return A builder for configuring and creating the sprite
	 */
	public static SpriteBuilder createSprite(String name) {
		return new SpriteBuilder(name);
	}

	/**
	 * Creates a new sprite map builder.
	 * @param name Base name of the sprite map
	 * @return A builder for configuring and creating the sprite map
	 */
	public static SpriteMapBuilder createSpriteMap(String name) {
		return new SpriteMapBuilder(name);
	}

	/**
	 * Creates a new animation builder.
	 * @param name Base name of the animation
	 * @return A builder for configuring and creating the animation
	 */
	public static AnimationBuilder createAnimation(String name) {
		return new AnimationBuilder(name);
	}

	/**
	 * Loads an existing animation by name.
	 * The animation must have been previously created.
	 *
	 * @param name The name of the animation to load
	 * @return The loaded animation
	 */
	public static SpriteAnimation getAnimation(String name) {
		SpriteAnimation animation = AssetManager.getInstance().getAssetByName(name, SpriteAnimation.class);
		return new SpriteAnimation(animation.getFrameReferences(), animation.getFrameDuration(), animation.isLooping());
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
	 * Preloads an asset of a specific type.
	 * This helps avoid type conflicts by being explicit about the asset type.
	 *
	 * @param name Name of the asset
	 * @param assetType The class of the asset type (e.g. SpriteMap.class)
	 * @return The loaded asset
	 */
	public static <T> T preloadAsType(String name, Class<T> assetType) {
		return AssetSystem.preloadAsType(name, assetType);
	}

	/**
	 * Unloads all unused assets.
	 */
	public static void unloadUnused() {
		AssetSystem.unloadUnused();
	}

	/**
	 * Creates a reference to a sprite.
	 *
	 * @param name Name of the sprite
	 * @return A reference to the sprite
	 */
	public static IAssetReference<Sprite> createSpriteReference(String name) {
		return AssetReferenceFactory.createReference(name, Sprite.class);
	}

	/**
	 * Creates a reference to a sprite within a sprite map.
	 *
	 * @param spriteMapName Name of the sprite map
	 * @param tileIndex Index of the tile within the sprite map
	 * @return A reference to the specific tile
	 */
	public static SpriteMapTileReference createSpriteMapTileReference(String spriteMapName, int tileIndex) {
		return AssetReferenceFactory.createSpriteMapTileReference(spriteMapName, tileIndex);
	}

	// Builder classes

	public static class SpriteBuilder {
		private final String name;
		private String imagePath;
		private double x = 0;
		private double y = 0;
		private double width = -1;
		private double height = -1;

		private SpriteBuilder(String name) {
			this.name = name;
			this.imagePath = name; // Default to using the name as path
		}

		/**
		 * Sets the image path for this sprite.
		 */
		public SpriteBuilder withImagePath(String path) {
			this.imagePath = path;
			return this;
		}

		/**
		 * Sets the source rectangle for this sprite.
		 */
		public SpriteBuilder withSourceRect(double x, double y, double width, double height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			return this;
		}

		/**
		 * Builds and loads the sprite.
		 */
		public Sprite load() {
			// Either load the entire image or a subregion
			if (width > 0 && height > 0) {
				// Load the sprite with a specific region
				Image image = AssetSystem.loadImage(imagePath);
				Sprite sprite = new Sprite(name, image, x, y, width, height);

				// Register with proper namespacing
				String namespacedId = AssetReferenceFactory.getNamespacedAssetId(name, Sprite.class);
				String imageId = AssetReferenceFactory.getNamespacedAssetId(imagePath, Image.class);

				AssetDescriptor<Sprite> descriptor = new AssetDescriptor<>(namespacedId, Sprite.class, name);
				descriptor.setMetadata("imageId", imageId);
				descriptor.setMetadata("x", x);
				descriptor.setMetadata("y", y);
				descriptor.setMetadata("width", width);
				descriptor.setMetadata("height", height);

				AssetManager.getInstance().registerAsset(descriptor);
				AssetManager.getInstance().storeAsset(namespacedId, sprite);

				return sprite;
			} else {
				return AssetSystem.loadSprite(name, imagePath);
			}
		}
	}

	public static class SpriteMapBuilder {
		private final String name;
		private String imagePath;
		private int columns = 0;
		private int rows = 0;
		private double tileWidth = 0;
		private double tileHeight = 0;
		private boolean autoDetectTileSize = false;

		private SpriteMapBuilder(String name) {
			this.name = name;
			this.imagePath = name; // Default to using the name as path
		}

		/**
		 * Sets the image path for this sprite map.
		 */
		public SpriteMapBuilder withImagePath(String path) {
			this.imagePath = path;
			return this;
		}

		/**
		 * Sets the grid dimensions.
		 */
		public SpriteMapBuilder withGrid(int columns, int rows, double tileWidth, double tileHeight) {
			this.columns = columns;
			this.rows = rows;
			this.tileWidth = tileWidth;
			this.tileHeight = tileHeight;
			this.autoDetectTileSize = false;
			return this;
		}

		/**
		 * Enables auto-detection of tile size.
		 */
		public SpriteMapBuilder withAutoDetectTileSize() {
			this.autoDetectTileSize = true;
			return this;
		}

		/**
		 * Builds and loads the sprite map.
		 */
		public SpriteMap load() {
			if (autoDetectTileSize) {
				return AssetSystem.loadSpriteSheet(name);
			} else {
				Image image = AssetSystem.loadImage(imagePath);
				return AssetSystem.defineSpriteSheet(name, imagePath, columns, rows, tileWidth, tileHeight);
			}
		}
	}

	public static class AnimationBuilder {
		private final String name;
		private List<String> frameNames;
		private SpriteMap spriteMap;
		private int[] tileIndices;
		private int startIndex = -1;
		private int endIndex = -1;
		private double frameDuration = 0.1;
		private boolean loop = true;

		private AnimationBuilder(String name) {
			this.name = name;
		}

		/**
		 * Sets specific frame names for this animation.
		 */
		public AnimationBuilder withFrames(String... frameNames) {
			this.frameNames = Arrays.asList(frameNames);
			return this;
		}

		/**
		 * Sets a sprite map to use for this animation.
		 */
		public AnimationBuilder withSpriteMap(SpriteMap spriteMap) {
			this.spriteMap = spriteMap;
			return this;
		}

		/**
		 * Sets specific tile indices from a sprite map.
		 */
		public AnimationBuilder withTileIndices(int... indices) {
			this.tileIndices = indices;
			return this;
		}

		/**
		 * Sets a range of tile indices from a sprite map.
		 */
		public AnimationBuilder withTileRange(int startIndex, int endIndex) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			return this;
		}

		/**
		 * Sets the frame duration.
		 */
		public AnimationBuilder withFrameDuration(double duration) {
			this.frameDuration = duration;
			return this;
		}

		/**
		 * Sets whether the animation should loop.
		 */
		public AnimationBuilder withLoop(boolean loop) {
			this.loop = loop;
			return this;
		}

		/**
		 * Builds and loads the animation.
		 */
		public SpriteAnimation load() {
			if (frameNames != null) {
				return AssetSystem.createAnimationWithPreloading(
					name, frameNames, frameDuration, loop);
			} else if (spriteMap != null) {
				if (tileIndices != null) {
					return AssetSystem.defineAnimationFromSpriteMap(
						name, spriteMap, tileIndices, frameDuration, loop);
				} else if (startIndex >= 0 && endIndex >= 0) {
					return AssetSystem.defineAnimationFromSpriteMap(
						name, spriteMap, startIndex, endIndex, frameDuration, loop);
				} else {
					return AssetSystem.defineAnimationFromSpriteMap(
						name, spriteMap, frameDuration, loop);
				}
			}
			throw new IllegalStateException("No valid configuration for animation: " + name);
		}
	}
}