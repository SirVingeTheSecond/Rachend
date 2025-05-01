package dk.sdu.sem.gamesystem.assets.loaders;

import dk.sdu.sem.gamesystem.assets.AssetDescriptor;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import javafx.scene.image.Image;

import java.io.InputStream;

/**
 * Loads Image assets from resources.
 */
public class ImageLoader implements IAssetLoader<Image> {
	private static final Logging LOGGER = Logging.createLogger("ImageLoader", LoggingLevel.DEBUG);

	@Override
	public Class<Image> getAssetType() {
		return Image.class;
	}

	@Override
	public Image loadAsset(AssetDescriptor<Image> descriptor) {
		try {
			String path = descriptor.getPath();
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);

			if (inputStream == null) {
				LOGGER.error("Resource not found: " + path);
				return null;
			}

			return new Image(inputStream);
		} catch (Exception e) {
			LOGGER.error("Failed to load image: " + descriptor.getPath());
			e.printStackTrace();
			return null;
		}
	}
}
