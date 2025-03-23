package dk.sdu.sem.gamesystem.assets.loaders;

import dk.sdu.sem.gamesystem.assets.AssetDescriptor;
import dk.sdu.sem.gamesystem.assets.managers.AssetManager;
import dk.sdu.sem.gamesystem.assets.references.SpriteReference;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads SpriteAnimation assets.
 */
public class SpriteAnimationLoader implements IAssetLoader<SpriteAnimation> {
	@Override
	public Class<SpriteAnimation> getAssetType() {
		return SpriteAnimation.class;
	}

	@Override
	public SpriteAnimation loadAsset(AssetDescriptor<SpriteAnimation> descriptor) {
		try {
			// Get the sprites for this animation
			List<String> spriteIds = (List<String>) descriptor.getMetadata("spriteIds");
			if (spriteIds == null || spriteIds.isEmpty()) {
				throw new IllegalArgumentException("Animation requires sprite IDs");
			}

			List<Sprite> frames = new ArrayList<>();
			for (String spriteId : spriteIds) {
				Sprite sprite = AssetManager.getInstance().getAsset(new SpriteReference(spriteId));
				frames.add(sprite);
			}

			Double frameDuration = (Double) descriptor.getMetadata("frameDuration");
			if (frameDuration == null) {
				frameDuration = 0.1; // Default
			}

			Boolean looping = (Boolean) descriptor.getMetadata("looping");
			if (looping == null) {
				looping = true; // Default
			}

			return new SpriteAnimation(frames, frameDuration, looping);
		} catch (Exception e) {
			System.err.println("Failed to load animation: " + descriptor.getId());
			e.printStackTrace();
			return null;
		}
	}
}
