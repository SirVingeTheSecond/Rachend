package dk.sdu.sem.gamesystem.assets.references;

import dk.sdu.sem.gamesystem.rendering.SpriteMap;

/**
 * Implementation of IAssetReference for SpriteMap assets.
 */
public class SpriteMapReference implements IAssetReference<SpriteMap> {
	private final String assetId;

	public SpriteMapReference(String assetId) {
		this.assetId = assetId;
	}

	@Override
	public String getAssetId() {
		return assetId;
	}

	@Override
	public Class<SpriteMap> getAssetType() {
		return SpriteMap.class;
	}
}