package dk.sdu.sem.collisionsystem.systems;

import dk.sdu.sem.collisionsystem.state.CollisionState;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

/**
 * Collision system that acts as a facade for the collision subsystems.
 * Implements IFixedUpdate to be called during the game loop.
 */
public class CollisionSystem implements IFixedUpdate {
	private static final Logging LOGGER = Logging.createLogger("CollisionSystem", LoggingLevel.DEBUG);

	// Shared state
	private final CollisionState collisionState;

	// Subsystems
	private final CollisionDetectionSystem detectionSystem;
	private final CollisionResolutionSystem resolutionSystem;
	private final CollisionEventSystem eventSystem;

	/**
	 * Creates a new collision system with all required subsystems.
	 */
	public CollisionSystem() {
		// Create shared state
		this.collisionState = new CollisionState();
		
		// Create subsystems with dependencies
		this.detectionSystem = new CollisionDetectionSystem(collisionState);
		this.resolutionSystem = new CollisionResolutionSystem(collisionState);
		this.eventSystem = new CollisionEventSystem(collisionState);
	}

	@Override
	public void fixedUpdate() {
		try {
			// Begin collision update
			collisionState.beginUpdate();

			// Process entity cleanup
			cleanupEntities();

			// Detect collisions
			detectionSystem.process();

			// Resolve physical collisions
			resolutionSystem.process();

			// Dispatch collision events
			eventSystem.process();
		} catch (Exception e) {
			LOGGER.error("Error in CollisionSystem.fixedUpdate: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Always end collision update, even if an error occurred
			collisionState.endUpdate();
		}
	}

	/**
	 * Cleans up collision state for destroyed entities.
	 */
	private void cleanupEntities() {
		for (var entity : collisionState.getAndClearCleanupEntities()) {
			// Delegate cleanup to subsystems
			detectionSystem.cleanupEntity(entity);
			eventSystem.cleanupEntity(entity);
		}
	}
}