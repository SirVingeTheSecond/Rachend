package dk.sdu.sem.gamesystem.assets.loaders;

import dk.sdu.sem.gamesystem.assets.AssetDescriptor;
import dk.sdu.sem.gamesystem.assets.AssetManager;
import dk.sdu.sem.gamesystem.assets.IAssetLoader;
import dk.sdu.sem.gamesystem.assets.ImageReference;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;
import javafx.scene.image.Image;

/**
 * Loads SpriteMap assets from image resources.
 */
public class SpriteMapLoader implements IAssetLoader<SpriteMap> {
	@Override
	public Class<SpriteMap> getAssetType() {
		return SpriteMap.class;
	}

	@Override
	public SpriteMap loadAsset(AssetDescriptor<SpriteMap> descriptor) {
		try {
			// Get the image for this sprite map
			Image image = AssetManager.getInstance().getAsset(
				new ImageReference(descriptor.getId() + "_image"));

			if (image == null) {
				return null;
			}

			SpriteMap spriteMap = new SpriteMap(descriptor.getId(), image);

			// Check if we need to define a grid
			Integer columns = (Integer) descriptor.getMetadata("columns");
			Integer rows = (Integer) descriptor.getMetadata("rows");
			Double spriteWidth = (Double) descriptor.getMetadata("spriteWidth");
			Double spriteHeight = (Double) descriptor.getMetadata("spriteHeight");

			if (columns != null && rows != null && spriteWidth != null && spriteHeight != null) {
				spriteMap.defineSpritesFromGrid(columns, rows, spriteWidth, spriteHeight);
			}

			return spriteMap;
		} catch (Exception e) {
			System.err.println("Failed to load sprite map: " + descriptor.getId());
			e.printStackTrace();
			return null;
		}
	}
}