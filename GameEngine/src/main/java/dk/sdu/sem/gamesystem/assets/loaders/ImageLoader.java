package dk.sdu.sem.gamesystem.assets.loaders;

import dk.sdu.sem.gamesystem.assets.AssetDescriptor;
import dk.sdu.sem.gamesystem.assets.IAssetLoader;
import javafx.scene.image.Image;

import java.io.InputStream;

/**
 * Loads Image assets from resources.
 */
public class ImageLoader implements IAssetLoader<Image> {
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
				System.err.println("Resource not found: " + path);
				return null;
			}

			return new Image(inputStream);
		} catch (Exception e) {
			System.err.println("Failed to load image: " + descriptor.getPath());
			e.printStackTrace();
			return null;
		}
	}
}
