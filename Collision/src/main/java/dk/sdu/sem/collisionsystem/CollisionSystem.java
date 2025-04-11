package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.ICollider;
import dk.sdu.sem.collision.RaycastResult;
import dk.sdu.sem.collisionsystem.raycasting.RaycastOptions;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;
import dk.sdu.sem.collisionsystem.detection.CollisionDetector;
import dk.sdu.sem.collisionsystem.resolution.CollisionResolver;
import dk.sdu.sem.collisionsystem.dispatching.TriggerDispatcher;
import dk.sdu.sem.collisionsystem.raycasting.RaycastHandler;

import java.util.Set;
import java.util.logging.Logger;

/**
 * Central system for collision detection and physics interactions.
 */
public class CollisionSystem implements ICollisionSPI, IFixedUpdate {
	private static final Logger LOGGER = Logger.getLogger(CollisionSystem.class.getName());

	private final CollisionDetector detector;
	private final CollisionResolver resolver;
	private final TriggerDispatcher triggerDispatcher;
	private final RaycastHandler raycastHandler;

	public CollisionSystem() {
		this.detector = new CollisionDetector();
		this.resolver = new CollisionResolver();
		this.triggerDispatcher = new TriggerDispatcher();
		this.raycastHandler = new RaycastHandler();

		LOGGER.info("CollisionSystem initialized");
	}

	@Override
	public void fixedUpdate() {
		// Get all collider nodes from active scene
		Set<ColliderNode> colliderNodes = NodeManager.active().getNodes(ColliderNode.class);

		// Phase 1: Detect collisions (broad and narrow phase)
		Set<CollisionPair> collisions = detector.detectCollisions(colliderNodes);

		// Phase 2: Dispatch trigger events
		triggerDispatcher.dispatchTriggerEvents(collisions);

		// Phase 3: Resolve physics collisions (non-triggers only)
		resolver.resolveCollisions(collisions);
	}

	@Override
	public boolean checkCollision(ICollider a, ICollider b) {
		return detector.checkCollision(a, b);
	}

	@Override
	public boolean checkTileCollision(ICollider collider, int tileX, int tileY, int tileSize) {
		return detector.checkTileCollision(collider, tileX, tileY, tileSize);
	}

	@Override
	public boolean isPositionValid(ICollider collider, Vector2D proposedPosition) {
		return detector.isPositionValid(collider, proposedPosition);
	}

	/**
	 * Performs a raycast in the specified direction.
	 *
	 * @param origin Starting point of the ray
	 * @param direction Direction of the ray
	 * @param maxDistance Maximum distance to check
	 * @return Information about what the ray hit, if anything
	 */
	public RaycastResult raycast(Vector2D origin, Vector2D direction, float maxDistance) {
		return raycastHandler.raycast(origin, direction, maxDistance);
	}

	/**
	 * Performs a dynamic raycast around a collider in the direction of movement.
	 *
	 * @param collider The collider to cast rays from
	 * @param direction Direction of movement
	 * @param options Raycasting options
	 * @return Array of results, one for each ray cast
	 */
	public RaycastResult[] castDynamicRays(ColliderNode collider, Vector2D direction, RaycastOptions options) {
		return raycastHandler.castDynamicRays(collider, direction, options);
	}

	/**
	 * Cleans up collision data for an entity being removed from the scene.
	 * This includes removing the entity from trigger tracking.
	 */
	public void cleanupEntity(Entity entity) {
		triggerDispatcher.removeEntityCollisions(entity);
	}
}