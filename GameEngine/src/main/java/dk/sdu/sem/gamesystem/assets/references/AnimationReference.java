package dk.sdu.sem.gamesystem.assets.references;

import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;

/**
 * Implementation of IAssetReference for SpriteAnimation assets.
 */
public class AnimationReference implements IAssetReference<SpriteAnimation> {
	private final String assetId;

	public AnimationReference(String assetId) {
		this.assetId = assetId;
	}

	@Override
	public String getAssetId() {
		return assetId;
	}

	@Override
	public Class<SpriteAnimation> getAssetType() {
		return SpriteAnimation.class;
	}
}
