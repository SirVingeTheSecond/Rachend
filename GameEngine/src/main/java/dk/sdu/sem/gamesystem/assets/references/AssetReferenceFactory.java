package dk.sdu.sem.gamesystem.assets.references;

import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;
import javafx.scene.image.Image;

/**
 * Factory for creating type-safe asset references from names.
 * Handles namespacing to prevent type collisions.
 */
public class AssetReferenceFactory {
	/**
	 * Creates an asset reference for the given name and type.
	 * Automatically applies appropriate namespace to prevent type collisions.
	 *
	 * @param name The asset base name (without type suffix)
	 * @param type The class of the asset
	 * @return A properly typed asset reference
	 * @throws IllegalArgumentException if the asset type is not supported
	 */
	@SuppressWarnings("unchecked")
	public static <T> IAssetReference<T> createReference(String name, Class<T> type) {
		String namespacedId = getNamespacedAssetId(name, type);

		if (type == Sprite.class) {
			return (IAssetReference<T>) new SpriteReference(namespacedId);
		} else if (type == Image.class) {
			return (IAssetReference<T>) new ImageReference(namespacedId);
		} else if (type == SpriteAnimation.class) {
			return (IAssetReference<T>) new AnimationReference(namespacedId);
		} else if (type == SpriteMap.class) {
			return (IAssetReference<T>) new SpriteMapReference(namespacedId);
		}

		throw new IllegalArgumentException("Unsupported asset type: " + type.getName());
	}

	/**
	 * Gets a namespaced asset ID to avoid collisions between asset types.
	 * Each asset type gets a unique suffix.
	 *
	 * @param baseName The base asset name (without type suffix)
	 * @param type The asset type
	 * @return A type-specific namespaced asset ID
	 */
	public static String getNamespacedAssetId(String baseName, Class<?> type) {
		// Remove any existing suffixes to prevent doubling
		String cleanName = getBaseNameFromId(baseName);

		if (type == Sprite.class) {
			return cleanName + "_sprite";
		} else if (type == Image.class) {
			return cleanName + "_img";
		} else if (type == SpriteMap.class) {
			return cleanName + "_sheet";
		} else if (type == SpriteAnimation.class) {
			return cleanName + "_anim";
		} else {
			return cleanName;
		}
	}

	/**
	 * Gets the base name from a namespaced asset ID.
	 * Removes type-specific suffixes, if present.
	 *
	 * @param namespacedId The potentially namespaced asset ID
	 * @return The base name without type suffix
	 */
	public static String getBaseNameFromId(String namespacedId) {
		if (namespacedId.endsWith("_sprite")) {
			return namespacedId.substring(0, namespacedId.length() - 7);
		} else if (namespacedId.endsWith("_img")) {
			return namespacedId.substring(0, namespacedId.length() - 4);
		} else if (namespacedId.endsWith("_sheet")) {
			return namespacedId.substring(0, namespacedId.length() - 6);
		} else if (namespacedId.endsWith("_anim")) {
			return namespacedId.substring(0, namespacedId.length() - 5);
		} else {
			return namespacedId;
		}
	}
}