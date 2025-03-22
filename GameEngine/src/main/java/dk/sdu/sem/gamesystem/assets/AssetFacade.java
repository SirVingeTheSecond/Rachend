package dk.sdu.sem.gamesystem.assets;

import dk.sdu.sem.gamesystem.assets.registry.AssetRegistrar;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade for the asset management system.
 */
public final class AssetFacade {
	// Private constructor to prevent instantiation
	private AssetFacade() {}

	// Core asset manager instance
	private static final AssetManager assetManager = AssetManager.getInstance();

	//--------------------------------------------------------------------------
	// Asset Loading Methods
	//--------------------------------------------------------------------------

	/**
	 * Gets an asset by ID, automatically determining the asset type.
	 * @param id The asset ID
	 * @return The loaded asset
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getAsset(String id) {
		IAssetReference<?> reference = createReferenceFromId(id);
		return (T) assetManager.getAsset(reference);
	}

	/**
	 * Gets an asset by ID with explicit type.
	 * @param id The asset ID
	 * @param assetType The class of the asset
	 * @return The loaded asset
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getAsset(String id, Class<T> assetType) {
		IAssetReference<?> reference;

		if (assetType == Image.class) {
			reference = new ImageReference(id);
		} else if (assetType == Sprite.class) {
			reference = new SpriteReference(id);
		} else if (assetType == SpriteAnimation.class) {
			reference = new AnimationReference(id);
		} else if (assetType == SpriteMap.class) {
			reference = new SpriteMapReference(id);
		} else {
			throw new IllegalArgumentException("Unsupported asset type: " + assetType.getName());
		}

		return (T) assetManager.getAsset(reference);
	}

	/**
	 * Creates the appropriate asset reference based on ID naming conventions.
	 */
	@SuppressWarnings("unchecked")
	public static <T> IAssetReference<T> createReferenceFromId(String id) {
		if (id.contains("_animation")) {
			return (IAssetReference<T>) new AnimationReference(id);
		} else if (id.contains("_map")) {
			return (IAssetReference<T>) new SpriteMapReference(id);
		} else if (id.contains("_image")) {
			return (IAssetReference<T>) new ImageReference(id);
		} else {
			// Default to sprite reference
			return (IAssetReference<T>) new SpriteReference(id);
		}
	}

	/**
	 * Creates a SpriteReference from an ID.
	 */
	public static SpriteReference createSpriteReference(String id) {
		return new SpriteReference(id);
	}

	/**
	 * Creates an AnimationReference from an ID.
	 */
	public static AnimationReference createAnimationReference(String id) {
		return new AnimationReference(id);
	}

	//--------------------------------------------------------------------------
	// Asset Registration Methods - Batch operations
	//--------------------------------------------------------------------------

	/**
	 * Registers a sprite sequence (images, sprites and animation) in one call.
	 *
	 * @param baseName Base name for the assets (e.g., "player_run")
	 * @param frameCount Number of frames in the sequence
	 * @param imagePathPattern Pattern for image paths with %d placeholder for frame number
	 * @param frameDuration Duration of each frame in seconds
	 * @param looping Whether the animation should loop
	 * @param registrar The registrar to use
	 * @return The ID of the created animation
	 */
	public static String registerSpriteSequence(
		String baseName,
		int frameCount,
		String imagePathPattern,
		double frameDuration,
		boolean looping,
		AssetRegistrar registrar) {

		List<String> spriteIds = new ArrayList<>(frameCount);

		for (int i = 0; i < frameCount; i++) {
			// Register image with your existing naming convention
			String imageId = baseName + "_image_" + i;
			String imagePath = String.format(imagePathPattern, i);
			registrar.registerImage(imageId, imagePath);

			// Register sprite with your existing naming convention
			String spriteId = baseName + "_" + i;  // Changed to match your convention
			registrar.registerSprite(spriteId, imageId);
			spriteIds.add(spriteId);
		}

		// Register animation
		String animationId = baseName + "_animation";
		registrar.registerAnimation(animationId, spriteIds, frameDuration, looping);

		return animationId;
	}

	/**
	 * Registers a sprite sheet as a grid and creates animations from it.
	 *
	 * @param baseName Base name for the assets
	 * @param imagePath Path to the sprite sheet image
	 * @param columns Number of columns in the sprite sheet
	 * @param rows Number of rows in the sprite sheet
	 * @param spriteWidth Width of each sprite in pixels
	 * @param spriteHeight Height of each sprite in pixels
	 * @param animationRows Array of row indices to create animations from
	 * @param frameDuration Duration of each frame in seconds
	 * @param looping Whether the animations should loop
	 * @param registrar The registrar to use
	 * @return Array of animation IDs created, one per row specified
	 */
	public static String[] registerSpriteSheet(
		String baseName,
		String imagePath,
		int columns,
		int rows,
		double spriteWidth,
		double spriteHeight,
		int[] animationRows,
		double frameDuration,
		boolean looping,
		AssetRegistrar registrar) {

		// Register the sprite sheet image
		String imageId = baseName + "_image";
		registrar.registerImage(imageId, imagePath);

		// Register the sprite map
		String mapId = baseName + "_map";
		registrar.registerSpriteMap(mapId, imageId, columns, rows, spriteWidth, spriteHeight);

		String[] animationIds = new String[animationRows.length];

		// Create animations for specified rows
		for (int i = 0; i < animationRows.length; i++) {
			int row = animationRows[i];
			List<String> rowSpriteIds = new ArrayList<>(columns);

			// Register sprites for this row
			for (int col = 0; col < columns; col++) {
				String spriteName = "tile_" + col + "_" + row;
				String spriteId = baseName + "_row" + row + "_sprite_" + col;
				registrar.registerSpriteFromMap(spriteId, mapId, spriteName);
				rowSpriteIds.add(spriteId);
			}

			// Register animation for this row
			String animationId = baseName + "_row" + row + "_animation";
			registrar.registerAnimation(animationId, rowSpriteIds, frameDuration, looping);
			animationIds[i] = animationId;
		}

		return animationIds;
	}

	//--------------------------------------------------------------------------
	// Asset Lifecycle Management Methods
	//--------------------------------------------------------------------------

	/**
	 * Preloads an asset by ID.
	 */
	public static <T> T preloadAsset(String assetId) {
		return assetManager.preloadAsset(assetId);
	}

	/**
	 * Releases a reference to an asset by ID.
	 */
	public static boolean releaseAsset(String assetId) {
		return assetManager.releaseAsset(assetId);
	}

	/**
	 * Unloads an asset by ID.
	 */
	public static void unloadAsset(String assetId) {
		assetManager.unloadAsset(assetId);
	}

	//--------------------------------------------------------------------------
	// Helper Methods
	//--------------------------------------------------------------------------

	/**
	 * Gets the ID of a sprite in a sequence.
	 *
	 * @param baseName The base name (e.g., "elf_idle")
	 * @param frameIndex The frame index (0-based)
	 * @return The sprite ID using your existing convention
	 */
	public static String getSpriteId(String baseName, int frameIndex) {
		return baseName + "_" + frameIndex;
	}

	/**
	 * Gets the ID of an animation.
	 *
	 * @param baseName The base name (e.g., "elf_idle")
	 * @return The animation ID
	 */
	public static String getAnimationId(String baseName) {
		return baseName + "_animation";
	}

	/**
	 * Creates a name for a row animation created by registerSpriteSheet.
	 *
	 * @param baseName The base name used in registerSpriteSheet
	 * @param rowIndex The row index
	 * @return The animation ID for that row
	 */
	public static String getRowAnimationId(String baseName, int rowIndex) {
		return baseName + "_row" + rowIndex + "_animation";
	}
}