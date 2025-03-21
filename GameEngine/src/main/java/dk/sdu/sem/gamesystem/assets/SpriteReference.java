package dk.sdu.sem.gamesystem.assets;

import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.assets.IAssetReference;

/**
 * Implementation of IAssetReference for Sprite assets.
 */
public class SpriteReference implements IAssetReference<Sprite> {
	private final String assetId;

	public SpriteReference(String assetId) {
		this.assetId = assetId;
	}

	@Override
	public String getAssetId() {
		return assetId;
	}

	@Override
	public Class<Sprite> getAssetType() {
		return Sprite.class;
	}
}
