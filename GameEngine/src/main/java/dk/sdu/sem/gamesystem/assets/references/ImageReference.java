package dk.sdu.sem.gamesystem.assets.references;

import javafx.scene.image.Image;

/**
 * Implementation of IAssetReference for Image assets.
 */
public class ImageReference implements IAssetReference<Image> {
	private final String assetId;

	public ImageReference(String assetId) {
		this.assetId = assetId;
	}

	@Override
	public String getAssetId() {
		return assetId;
	}

	@Override
	public Class<Image> getAssetType() {
		return Image.class;
	}
}