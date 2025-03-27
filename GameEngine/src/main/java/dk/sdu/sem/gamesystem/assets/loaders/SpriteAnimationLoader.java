package dk.sdu.sem.gamesystem.assets.loaders;

import dk.sdu.sem.gamesystem.assets.AssetDescriptor;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.assets.references.SpriteReference;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads SpriteAnimation assets using sprite references.
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

			// Create sprite references instead of loading the sprites directly
			List<IAssetReference<Sprite>> frameReferences = new ArrayList<>();
			for (String spriteId : spriteIds) {
				// Create a reference to the sprite instead of loading it
				frameReferences.add(new SpriteReference(spriteId));
			}

			Double frameDuration = (Double) descriptor.getMetadata("frameDuration");
			if (frameDuration == null) {
				frameDuration = 0.1; // Default
			}

			Boolean looping = (Boolean) descriptor.getMetadata("looping");
			if (looping == null) {
				looping = true; // Default
			}

			// Create animation with references
			return new SpriteAnimation(frameReferences, frameDuration, looping);
		} catch (Exception e) {
			System.err.println("Failed to load animation: " + descriptor.getId());
			e.printStackTrace();
			return null;
		}
	}
}