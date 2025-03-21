package dk.sdu.sem.gamesystem.assets.registry;

import dk.sdu.sem.gamesystem.assets.AssetDescriptor;
import dk.sdu.sem.gamesystem.assets.AssetManager;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;
import javafx.scene.image.Image;

import java.util.List;

/**
 * Handles registration of assets.
 */
public class AssetRegistrar {
	private final AssetManager assetManager;

	public AssetRegistrar(AssetManager assetManager) {
		this.assetManager = assetManager;
	}

	/**
	 * Registers an image asset
	 */
	public void registerImage(String id, String path) {
		AssetDescriptor<Image> descriptor = new AssetDescriptor<>(id, Image.class, path);
		assetManager.registerAsset(descriptor);
	}

	/**
	 * Registers a standalone sprite asset
	 */
	public void registerSprite(String id, String imageId) {
		AssetDescriptor<Sprite> descriptor = new AssetDescriptor<>(id, Sprite.class, null);
		descriptor.setMetadata("imageId", imageId);
		assetManager.registerAsset(descriptor);
	}

	/**
	 * Registers a sprite with a source rectangle
	 */
	public void registerSprite(String id, String imageId, double x, double y, double width, double height) {
		AssetDescriptor<Sprite> descriptor = new AssetDescriptor<>(id, Sprite.class, null);
		descriptor.setMetadata("imageId", imageId);
		descriptor.setMetadata("x", x);
		descriptor.setMetadata("y", y);
		descriptor.setMetadata("width", width);
		descriptor.setMetadata("height", height);
		assetManager.registerAsset(descriptor);
	}

	/**
	 * Registers a sprite map with grid layout
	 */
	public void registerSpriteMap(String id, String imageId, int columns, int rows, double spriteWidth, double spriteHeight) {
		AssetDescriptor<SpriteMap> descriptor = new AssetDescriptor<>(id, SpriteMap.class, null);
		descriptor.setMetadata("imageId", imageId);
		descriptor.setMetadata("columns", columns);
		descriptor.setMetadata("rows", rows);
		descriptor.setMetadata("spriteWidth", spriteWidth);
		descriptor.setMetadata("spriteHeight", spriteHeight);
		assetManager.registerAsset(descriptor);
	}

	/**
	 * Registers a sprite from a sprite map
	 */
	public void registerSpriteFromMap(String id, String spriteMapId, String spriteName) {
		AssetDescriptor<Sprite> descriptor = new AssetDescriptor<>(id, Sprite.class, null);
		descriptor.setMetadata("spriteMapId", spriteMapId);
		descriptor.setMetadata("spriteName", spriteName);
		assetManager.registerAsset(descriptor);
	}

	/**
	 * Registers a sprite animation
	 */
	public void registerAnimation(String id, List<String> spriteIds, double frameDuration, boolean looping) {
		AssetDescriptor<SpriteAnimation> descriptor = new AssetDescriptor<>(id, SpriteAnimation.class, null);
		descriptor.setMetadata("spriteIds", spriteIds);
		descriptor.setMetadata("frameDuration", frameDuration);
		descriptor.setMetadata("looping", looping);
		assetManager.registerAsset(descriptor);
	}
}