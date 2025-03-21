package dk.sdu.sem.gamesystem.assets.loaders;

import dk.sdu.sem.gamesystem.assets.*;
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
			// Check if this is a sub-sprite from a sprite map
			String spriteMapId = (String) descriptor.getMetadata("spriteMapId");
			if (spriteMapId != null) {
				SpriteMap spriteMap = AssetManager.getInstance().getAsset(
					new SpriteMapReference(spriteMapId));

				String spriteName = (String) descriptor.getMetadata("spriteName");
				return spriteMap.getSprite(spriteName);
			}

			// Otherwise, it's a standalone sprite
			Image image = AssetManager.getInstance().getAsset(
				new ImageReference(descriptor.getId() + "_image"));

			Double x = (Double) descriptor.getMetadata("x");
			Double y = (Double) descriptor.getMetadata("y");
			Double width = (Double) descriptor.getMetadata("width");
			Double height = (Double) descriptor.getMetadata("height");

			if (x != null && y != null && width != null && height != null) {
				return new Sprite(descriptor.getId(), image, x, y, width, height);
			} else {
				return new Sprite(descriptor.getId(), image);
			}
		} catch (Exception e) {
			System.err.println("Failed to load sprite: " + descriptor.getId());
			e.printStackTrace();
			return null;
		}
	}
}