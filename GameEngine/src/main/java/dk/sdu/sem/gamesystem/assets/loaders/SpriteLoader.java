package dk.sdu.sem.gamesystem.assets.loaders;

import dk.sdu.sem.gamesystem.assets.*;
import dk.sdu.sem.gamesystem.assets.managers.AssetManager;
import dk.sdu.sem.gamesystem.assets.references.ImageReference;
import dk.sdu.sem.gamesystem.assets.references.SpriteMapReference;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;
import javafx.scene.image.Image;

/**
 * Loads Sprite assets from image resources.
 */
public class SpriteLoader implements IAssetLoader<Sprite> {
	@Override
	public Class<Sprite> getAssetType() {
		return Sprite.class;
	}

	@Override
	public Sprite loadAsset(AssetDescriptor<Sprite> descriptor) {
		try {
			System.out.println("SpriteLoader: Loading sprite: " + descriptor.getId());

			// CASE 1: Loading a sprite from a sprite map
			String spriteMapId = (String) descriptor.getMetadata("spriteMapId");
			if (spriteMapId != null) {
				System.out.println("SpriteLoader: Loading from sprite map: " + spriteMapId);

				SpriteMap spriteMap = AssetManager.getInstance().getAsset(
					new SpriteMapReference(spriteMapId));

				if (spriteMap == null) {
					System.err.println("Error: SpriteMap not found: " + spriteMapId);
					return null;
				}

				String spriteName = (String) descriptor.getMetadata("spriteName");

				if (spriteName == null) {
					System.err.println("Error: No spriteName metadata for sprite: " + descriptor.getId());
					return null;
				}

				Sprite sprite = spriteMap.getSprite(spriteName);

				if (sprite == null) {
					System.err.println("Error: Sprite not found in map: " + spriteName);
					return null;
				}

				return sprite;
			}

			// CASE 2: Loading a standalone sprite from an image
			// Get the imageId directly from metadata
			String imageId = (String) descriptor.getMetadata("imageId");
			if (imageId == null) {
				System.err.println("Error: No imageId metadata for sprite: " + descriptor.getId());
				return null;
			}

			System.out.println("SpriteLoader: Using image: " + imageId);

			// Load the image using the provided ID
			Image image = AssetManager.getInstance().getAsset(new ImageReference(imageId));

			if (image == null) {
				System.err.println("Error: Failed to load image: " + imageId);
				return null;
			}

			// Check if we need to create a sub-region of the image
			Double x = (Double) descriptor.getMetadata("x");
			Double y = (Double) descriptor.getMetadata("y");
			Double width = (Double) descriptor.getMetadata("width");
			Double height = (Double) descriptor.getMetadata("height");

			// Create the sprite using either a region or the whole image
			if (x != null && y != null && width != null && height != null) {
				System.out.println("SpriteLoader: Creating sprite with region: " + x + "," + y + "," + width + "," + height);
				return new Sprite(descriptor.getId(), image, x, y, width, height);
			} else {
				System.out.println("SpriteLoader: Creating sprite with full image");
				return new Sprite(descriptor.getId(), image);
			}
		} catch (Exception e) {
			System.err.println("Error loading sprite: " + descriptor.getId());
			e.printStackTrace();
			return null;
		}
	}
}