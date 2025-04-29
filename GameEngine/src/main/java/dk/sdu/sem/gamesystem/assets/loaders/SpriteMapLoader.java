package dk.sdu.sem.gamesystem.assets.loaders;

import dk.sdu.sem.gamesystem.assets.AssetDescriptor;
import dk.sdu.sem.gamesystem.assets.managers.AssetManager;
import dk.sdu.sem.gamesystem.assets.references.ImageReference;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import javafx.scene.image.Image;

/**
 * Loads SpriteMap assets from image resources.
 */
public class SpriteMapLoader implements IAssetLoader<SpriteMap> {
	private static final Logging LOGGER = Logging.createLogger("SpriteMapLoader", LoggingLevel.DEBUG);

	@Override
	public Class<SpriteMap> getAssetType() {
		return SpriteMap.class;
	}

	@Override
	public SpriteMap loadAsset(AssetDescriptor<SpriteMap> descriptor) {
		try {
			LOGGER.debug("SpriteMapLoader: Loading sprite map: " + descriptor.getId());

			// Get the image ID directly from metadata
			String imageId = (String) descriptor.getMetadata("imageId");
			if (imageId == null) {
				LOGGER.error("Error: No imageId metadata for sprite map: " + descriptor.getId());
				return null;
			}

			LOGGER.debug("SpriteMapLoader: Using image: " + imageId);

			// Load the image using the provided ID
			Image image = AssetManager.getInstance().getAsset(new ImageReference(imageId));

			if (image == null) {
				LOGGER.error("Error: Failed to load image: " + imageId);
				return null;
			}

			// Create the sprite map
			SpriteMap spriteMap = new SpriteMap(descriptor.getId(), image);

			// Check if we need to define a grid
			Integer columns = (Integer) descriptor.getMetadata("columns");
			Integer rows = (Integer) descriptor.getMetadata("rows");
			Double spriteWidth = (Double) descriptor.getMetadata("spriteWidth");
			Double spriteHeight = (Double) descriptor.getMetadata("spriteHeight");

			if (columns != null && rows != null && spriteWidth != null && spriteHeight != null) {
				LOGGER.debug("SpriteMapLoader: Defining grid: " + columns + "x" + rows +
					" with sprite size " + spriteWidth + "x" + spriteHeight);
				spriteMap.defineSpritesFromGrid(columns, rows, spriteWidth, spriteHeight);
			}

			return spriteMap;
		} catch (Exception e) {
			LOGGER.error("Error loading sprite map: " + descriptor.getId());
			e.printStackTrace();
			return null;
		}
	}
}