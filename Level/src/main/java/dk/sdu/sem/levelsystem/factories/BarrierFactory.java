package dk.sdu.sem.levelsystem.factories;

import dk.sdu.sem.collision.components.CollisionStateComponent;
import dk.sdu.sem.collision.components.TilemapColliderComponent;
import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commontilemap.TilemapComponent;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.components.TilemapRendererComponent;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

import java.util.Arrays;

/**
 * Factory for creating barrier entities around room perimeters.
 * Handles creation of collision maps and components.
 */
public class BarrierFactory implements IBarrierFactory {
	private static final Logging LOGGER = Logging.createLogger("BarrierFactory", LoggingLevel.DEBUG);

	private final BarrierAnimationFactory barrierAnimationFactory;
	private final BarrierPopulator barrierPopulator;

	/**
	 * Default constructor - creates factory with default dependencies.
	 */
	public BarrierFactory() {
		this(new BarrierAnimationFactory(), new BarrierPopulator());
	}

	/**
	 * Creates a new barrier factory with the specified dependencies.
	 *
	 * @param barrierAnimationFactory Factory for adding animations
	 * @param barrierPopulator Service for populating barrier maps
	 */
	public BarrierFactory(BarrierAnimationFactory barrierAnimationFactory, BarrierPopulator barrierPopulator) {
		this.barrierAnimationFactory = barrierAnimationFactory;
		this.barrierPopulator = barrierPopulator;
	}

	@Override
	public Entity createBarrier(Room room, SolidityChecker solidityChecker) {
		LOGGER.debug("Creating barrier entity");

		try {
			Entity barrier = new Entity();

			// Add transform component
			barrier.addComponent(new TransformComponent(Vector2D.ZERO, 0));

			// Create maps for collision and rendering
			int[][] collisionMap = new int[(int)GameConstants.WORLD_SIZE.x()][(int)GameConstants.WORLD_SIZE.y()];
			int[][] renderMap = new int[(int)GameConstants.WORLD_SIZE.x()][(int)GameConstants.WORLD_SIZE.y()];

			// Initialize render map with -1 (no tile)
			for (int[] r : renderMap) {
				Arrays.fill(r, -1);
			}

			// Populate maps based on collision checks
			barrierPopulator.populateMaps(room, collisionMap, renderMap, solidityChecker);

			// Add tilemap component and renderer
			TilemapComponent tilemap = new TilemapComponent(
				"force-field",
				renderMap,
				GameConstants.TILE_SIZE
			);
			barrier.addComponent(tilemap);

			TilemapRendererComponent renderer = new TilemapRendererComponent(tilemap);
			renderer.setRenderLayer(GameConstants.LAYER_UI);
			barrier.addComponent(renderer);

			// Add animation component
			barrierAnimationFactory.addAnimationComponent(barrier);

			// Add collision components
			TilemapColliderComponent colliderComponent = new TilemapColliderComponent(
				barrier,
				collisionMap,
				GameConstants.TILE_SIZE
			);
			barrier.addComponent(colliderComponent);
			barrier.addComponent(new CollisionStateComponent());

			return barrier;
		} catch (Exception e) {
			LOGGER.error("Error creating barrier: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
}