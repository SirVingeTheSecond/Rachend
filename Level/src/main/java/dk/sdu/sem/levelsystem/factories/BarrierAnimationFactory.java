package dk.sdu.sem.levelsystem.factories;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.gamesystem.animation.TileAnimation;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.components.TileAnimatorComponent;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for handling barrier entity animations.
 */
public class BarrierAnimationFactory {
	private static final Logging LOGGER = Logging.createLogger("BarrierAnimationService", LoggingLevel.DEBUG);

	/**
	 * Adds tile animation component to the barrier entity.
	 *
	 * @param entity The barrier entity
	 */
	public void addAnimationComponent(Entity entity) {
		try {
			// Create animation component
			TileAnimatorComponent animator = new TileAnimatorComponent();

			// Get all frames from the force-field sprite map
			List<IAssetReference<Sprite>> animatedSprites = new ArrayList<>();
			List<Float> frameDurations = new ArrayList<>();

			// Get the animation from the asset system
			for (int i = 0; i < 8; i++) {
				animatedSprites.add(
					AssetFacade.createSpriteMapTileReference("force-field", i)
				);
				frameDurations.add(0.1f);
			}

			TileAnimation tileAnimation = new TileAnimation(animatedSprites, frameDurations, true);
			animator.addTileAnimation(0, tileAnimation);
			entity.addComponent(animator);
		} catch (Exception e) {
			LOGGER.error("Error creating tile animation: " + e.getMessage());
			e.printStackTrace();
		}
	}
}