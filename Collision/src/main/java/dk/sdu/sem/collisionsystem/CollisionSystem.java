package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.ICollider;
import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.ITriggerListener;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.components.TilemapColliderComponent;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.collision.shapes.RectangleShape;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Pair;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.TilemapComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System that handles collision detection and resolution with Unity-like trigger callbacks.
 * Uses spatial partitioning for efficiency.
 */
public class CollisionSystem implements ICollisionSPI, IFixedUpdate {
	private static final Logger LOGGER = Logger.getLogger(CollisionSystem.class.getName());

	// Constants for configuration
	private static final float COLLISION_THRESHOLD = 0.03f;
	private static final boolean DEBUG = false;
	private static final float FAST_VELOCITY_THRESHOLD = 500.0f; // Units per second

	// Cache for non-colliding tiles to improve performance
	private final Map<Pair<Integer, Integer>, Boolean> nonCollidingTileCache = new HashMap<>();

	// Collision tracking for trigger events
	private final Object collisionLock = new Object();
	private Set<Pair<Entity, Entity>> activeCollisions =
		Collections.newSetFromMap(new ConcurrentHashMap<>());
	private Set<Pair<Entity, Entity>> previousFrameCollisions =
		Collections.newSetFromMap(new ConcurrentHashMap<>());

	// Physics layer matrix for collision filtering
	private final LayerCollisionMatrix layerMatrix = new LayerCollisionMatrix();

	/**
	 * Creates a new collision system.
	 */
	public CollisionSystem() {
		LOGGER.info("CollisionSystem initialized with Unity-like trigger system");
	}

	/**
	 * Estimates the world bounds for spatial partitioning.
	 */
	private AABB getWorldBounds() {
		// Default to a reasonably large area if no entities found
		float minX = -1000;
		float minY = -1000;
		float maxX = 1000;
		float maxY = 1000;

		// Get all colliders to calculate actual bounds
		Set<ColliderNode> colliders = NodeManager.active().getNodes(ColliderNode.class);
		if (!colliders.isEmpty()) {
			boolean initialized = false;

			for (ColliderNode node : colliders) {
				if (node == null || node.getEntity() == null || node.transform == null) {
					continue;
				}

				Vector2D pos = node.transform.getPosition();

				// Initialize with first valid position
				if (!initialized) {
					minX = pos.getX() - 500;
					minY = pos.getY() - 500;
					maxX = pos.getX() + 500;
					maxY = pos.getY() + 500;
					initialized = true;
				} else {
					// Expand bounds to include this entity
					minX = Math.min(minX, pos.getX() - 500);
					minY = Math.min(minY, pos.getY() - 500);
					maxX = Math.max(maxX, pos.getX() + 500);
					maxY = Math.max(maxY, pos.getY() + 500);
				}
			}
		}

		return new AABB(minX, minY, maxX, maxY);
	}

	@Override
	public void fixedUpdate() {
		// Process all entity-to-entity collisions with spatial optimization
		detectEntityCollisions();

		// Process entity-tilemap collisions
		resolveCollisions();
	}

	/**
	 * Detects and processes entity-to-entity collisions using spatial partitioning.
	 */
	private void detectEntityCollisions() {
		// Get all collider nodes
		Set<ColliderNode> colliderNodes = NodeManager.active().getNodes(ColliderNode.class);

		// Skip processing if there are fewer than 2 colliders
		if (colliderNodes.size() < 2) {
			return;
		}

		// Create a temporary copy to avoid concurrent modification
		List<ColliderNode> validNodes = new ArrayList<>();
		for (ColliderNode node : colliderNodes) {
			if (isNodeValid(node)) {
				validNodes.add(node);
			}
		}

		// Create a quadtree for this frame
		QuadTree quadTree = new QuadTree(getWorldBounds(), 0);

		// Insert all entities into the quadtree
		for (ColliderNode node : validNodes) {
			quadTree.insert(node);
		}

		// Process collisions using spatial optimizations
		synchronized(collisionLock) {
			// Swap collision sets for tracking enter/exit events
			Set<Pair<Entity, Entity>> tmp = previousFrameCollisions;
			previousFrameCollisions = activeCollisions;
			activeCollisions = tmp;
			activeCollisions.clear();

			// Check each entity against potential collision partners
			for (ColliderNode nodeA : validNodes) {
				Entity entityA = nodeA.getEntity();
				ColliderComponent colliderA = nodeA.collider;

				// Get potential collisions from quadtree (O(log n) instead of O(n))
				Set<ColliderNode> potentialCollisions = quadTree.getPotentialCollisions(nodeA);

				for (ColliderNode nodeB : potentialCollisions) {
					if (nodeB == nodeA || !isNodeValid(nodeB)) {
						continue;
					}

					Entity entityB = nodeB.getEntity();
					ColliderComponent colliderB = nodeB.collider;

					// Skip based on trigger rules and layer matrix
					if (!shouldProcessCollision(colliderA, colliderB)) {
						continue;
					}

					// Perform detailed collision test
					boolean collides = false;

					// Fast moving objects need continuous collision detection
					PhysicsComponent physicsA = entityA.getComponent(PhysicsComponent.class);
					PhysicsComponent physicsB = entityB.getComponent(PhysicsComponent.class);

					if (isFastMoving(physicsA) || isFastMoving(physicsB)) {
						collides = checkContinuousCollision(nodeA, nodeB, (float)Time.getFixedDeltaTime());
					} else {
						collides = checkCollision(colliderA, colliderB);
					}

					if (collides) {
						handleCollision(entityA, entityB, colliderA, colliderB);
					}
				}
			}

			// Process collision exits
			handleCollisionExits();
		}
	}

	/**
	 * Checks if a physics component represents a fast-moving entity.
	 */
	private boolean isFastMoving(PhysicsComponent physics) {
		if (physics == null) {
			return false;
		}
		return physics.getVelocity().magnitude() > FAST_VELOCITY_THRESHOLD;
	}

	/**
	 * Checks for continuous collision detection between fast-moving entities.
	 */
	private boolean checkContinuousCollision(ColliderNode nodeA, ColliderNode nodeB, float deltaTime) {
		// Simple implementation - check both current positions and projected future positions
		if (checkCollision(nodeA.collider, nodeB.collider)) {
			return true;
		}

		// Project future positions for both entities
		Entity entityA = nodeA.getEntity();
		Entity entityB = nodeB.getEntity();
		PhysicsComponent physicsA = entityA.getComponent(PhysicsComponent.class);
		PhysicsComponent physicsB = entityB.getComponent(PhysicsComponent.class);

		if (physicsA != null) {
			Vector2D posA = nodeA.transform.getPosition();
			Vector2D velA = physicsA.getVelocity();
			Vector2D futureA = posA.add(velA.scale(deltaTime));

			// Create a ray from current to future position
			Ray ray = new Ray(posA, futureA.subtract(posA).normalize());
			float distance = posA.distance(futureA);

			// Check if ray intersects with the other collider
			if (raycastCollider(ray, distance, nodeB.collider)) {
				return true;
			}
		}

		if (physicsB != null) {
			Vector2D posB = nodeB.transform.getPosition();
			Vector2D velB = physicsB.getVelocity();
			Vector2D futureB = posB.add(velB.scale(deltaTime));

			// Create a ray from current to future position
			Ray ray = new Ray(posB, futureB.subtract(posB).normalize());
			float distance = posB.distance(futureB);

			// Check if ray intersects with the other collider
			if (raycastCollider(ray, distance, nodeA.collider)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Performs a raycast against a collider.
	 */
	private boolean raycastCollider(Ray ray, float maxDistance, ColliderComponent collider) {
		// Simple implementation for circle shapes
		if (collider.getCollisionShape() instanceof CircleShape) {
			CircleShape circle = (CircleShape) collider.getCollisionShape();
			Vector2D center = collider.getEntity().getComponent(TransformComponent.class).getPosition()
				.add(collider.getOffset());

			// Vector from ray origin to circle center
			Vector2D toCenter = center.subtract(ray.getOrigin());

			// Project this vector onto the ray direction
			float projection = toCenter.getX() * ray.getDirection().getX() +
				toCenter.getY() * ray.getDirection().getY();

			// If negative, circle is behind ray
			if (projection < 0) {
				return false;
			}

			// If projection is greater than max distance, circle is too far
			if (projection > maxDistance) {
				return false;
			}

			// Find closest point on ray to circle center
			Vector2D closestPoint = ray.getOrigin().add(ray.getDirection().scale(projection));

			// Check if closest point is within circle radius
			float distanceSquared = closestPoint.subtract(center).magnitudeSquared();
			return distanceSquared <= circle.getRadius() * circle.getRadius();
		}

		// Simple implementation for rectangle shapes
		if (collider.getCollisionShape() instanceof RectangleShape) {
			RectangleShape rect = (RectangleShape) collider.getCollisionShape();

			// Convert rectangle to AABB
			AABB aabb = new AABB(
				rect.getPosition().getX(),
				rect.getPosition().getY(),
				rect.getPosition().getX() + rect.getWidth(),
				rect.getPosition().getY() + rect.getHeight()
			);

			// Ray-AABB intersection test
			Vector2D invDir = new Vector2D(
				1.0f / ray.getDirection().getX(),
				1.0f / ray.getDirection().getY()
			);

			float t1 = (aabb.getMinX() - ray.getOrigin().getX()) * invDir.getX();
			float t2 = (aabb.getMaxX() - ray.getOrigin().getX()) * invDir.getX();
			float t3 = (aabb.getMinY() - ray.getOrigin().getY()) * invDir.getY();
			float t4 = (aabb.getMaxY() - ray.getOrigin().getY()) * invDir.getY();

			float tmin = Math.max(Math.min(t1, t2), Math.min(t3, t4));
			float tmax = Math.min(Math.max(t1, t2), Math.max(t3, t4));

			// Ray intersects AABB if tmax is positive and tmin <= tmax
			return tmax >= 0 && tmin <= tmax && tmin <= maxDistance;
		}

		// Default conservative approach for other shape types
		return false;
	}

	/**
	 * Checks if a node is valid for collision detection.
	 */
	private boolean isNodeValid(ColliderNode node) {
		return node != null &&
			node.getEntity() != null &&
			node.getEntity().getScene() != null &&
			node.transform != null &&
			node.collider != null;
	}

	/**
	 * Determines if a collision between two colliders should be processed.
	 */
	private boolean shouldProcessCollision(ColliderComponent colliderA, ColliderComponent colliderB) {
		// Skip trigger-trigger interactions (Unity-like behavior)
		if (colliderA.isTrigger() && colliderB.isTrigger()) {
			return false;
		}

		// Check layer collision matrix
		return layerMatrix.canLayersCollide(colliderA.getLayer(), colliderB.getLayer());
	}

	/**
	 * Handles a detected collision between two entities.
	 */
	private void handleCollision(Entity entityA, Entity entityB,
								 ColliderComponent colliderA, ColliderComponent colliderB) {
		System.out.println("COLLISION DETECTED between: " +
			entityA.getID() + " and " + entityB.getID());
		System.out.println("isTrigger A: " + colliderA.isTrigger() +
			", isTrigger B: " + colliderB.isTrigger());
		System.out.println("Layer A: " + colliderA.getLayer() +
			", Layer B: " + colliderB.getLayer());

		try {
			// Create a unique identifier for this collision pair
			// Use canonical ordering to ensure consistency between frames
			Pair<Entity, Entity> pair;
			if (entityA.getID().compareTo(entityB.getID()) < 0) {
				pair = new Pair<>(entityA, entityB);
			} else {
				pair = new Pair<>(entityB, entityA);
			}

			// Check if this is the first frame of collision
			boolean isCollisionStart = !previousFrameCollisions.contains(pair);

			// Record this collision
			activeCollisions.add(pair);

			if (DEBUG) {
				LOGGER.fine("Collision between " + entityA.getID() + " and " + entityB.getID() +
					" (start: " + isCollisionStart + ")");
			}

			// Handle trigger collisions
			if (colliderA.isTrigger()) {
				notifyTriggerEvent(entityA, entityB, isCollisionStart, false);
			}

			if (colliderB.isTrigger()) {
				notifyTriggerEvent(entityB, entityA, isCollisionStart, false);
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error handling collision: " + e.getMessage(), e);
		}
	}

	/**
	 * Handles collisions that are no longer active.
	 */
	private void handleCollisionExits() {
		for (Pair<Entity, Entity> pair : previousFrameCollisions) {
			if (!activeCollisions.contains(pair)) {
				Entity entityA = pair.getFirst();
				Entity entityB = pair.getSecond();

				// Skip if either entity has been removed
				if (entityA.getScene() == null || entityB.getScene() == null) {
					continue;
				}

				ColliderComponent colliderA = entityA.getComponent(ColliderComponent.class);
				ColliderComponent colliderB = entityB.getComponent(ColliderComponent.class);

				// Skip if colliders are missing
				if (colliderA == null || colliderB == null) {
					continue;
				}

				// Notify triggers about exit events
				if (colliderA.isTrigger()) {
					notifyTriggerEvent(entityA, entityB, false, true);
				}

				if (colliderB.isTrigger()) {
					notifyTriggerEvent(entityB, entityA, false, true);
				}
			}
		}
	}

	/**
	 * Notifies components about trigger events.
	 *
	 * @param triggerEntity The entity with the trigger collider
	 * @param otherEntity The entity that entered/stayed/exited the trigger
	 * @param isCollisionStart True if this is the first frame of collision
	 * @param isExit True if this is an exit event
	 */
	private void notifyTriggerEvent(Entity triggerEntity, Entity otherEntity,
									boolean isCollisionStart, boolean isExit) {
		try {
			// UNITY-LIKE BEHAVIOR: Find ITriggerListener components ON THE TRIGGER ENTITY
			for (IComponent component : triggerEntity.getAllComponents()) {
				if (component instanceof ITriggerListener) {
					ITriggerListener listener = (ITriggerListener) component;

					// Call the appropriate event method
					if (isExit) {
						listener.onTriggerExit(otherEntity);
					} else if (isCollisionStart) {
						listener.onTriggerEnter(otherEntity);
					} else {
						listener.onTriggerStay(otherEntity);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error in trigger event notification: " + e.getMessage(), e);
		}
	}

	/**
	 * Resolves physics collisions between entities and tilemaps.
	 */
	private void resolveCollisions() {
		// Get all entities with physics and colliders
		Set<PhysicsColliderNode> physicsNodes = NodeManager.active().getNodes(PhysicsColliderNode.class);

		// Get all tilemap colliders
		Set<TilemapColliderNode> tilemapNodes = NodeManager.active().getNodes(TilemapColliderNode.class);

		// Skip if either set is empty
		if (physicsNodes.isEmpty() || tilemapNodes.isEmpty()) {
			return;
		}

		// Process each physics entity against each tilemap
		for (PhysicsColliderNode physicsNode : physicsNodes) {
			// Skip if not moving
			if (physicsNode.physicsComponent.getVelocity().magnitudeSquared() < 0.001f) {
				continue;
			}

			// Get current state
			Vector2D currentPos = physicsNode.transform.getPosition();
			Vector2D velocity = physicsNode.physicsComponent.getVelocity();
			float deltaTime = (float) Time.getFixedDeltaTime();

			// Split movement into X and Y components for better collision handling
			Vector2D xMovement = new Vector2D(velocity.getX() * deltaTime, 0);
			Vector2D yMovement = new Vector2D(0, velocity.getY() * deltaTime);

			// Initialize collision flags
			boolean xCollision = false;
			boolean yCollision = false;

			// Try X movement first
			Vector2D proposedXPos = currentPos.add(xMovement);
			for (TilemapColliderNode tilemapNode : tilemapNodes) {
				if (testTilemapCollision(physicsNode, tilemapNode, proposedXPos)) {
					xCollision = true;
					break;
				}
			}

			// Try Y movement from the updated position
			Vector2D testPos = xCollision ? currentPos : proposedXPos;
			Vector2D proposedYPos = testPos.add(yMovement);
			for (TilemapColliderNode tilemapNode : tilemapNodes) {
				if (testTilemapCollision(physicsNode, tilemapNode, proposedYPos)) {
					yCollision = true;
					break;
				}
			}

			// Now we only modify velocity to account for collisions
			Vector2D newVelocity = velocity;
			if (xCollision) {
				newVelocity = new Vector2D(0, newVelocity.getY());
			}
			if (yCollision) {
				newVelocity = new Vector2D(newVelocity.getX(), 0);
			}

			physicsNode.physicsComponent.setVelocity(newVelocity);
		}
	}

	/**
	 * Tests if an entity would collide with a tilemap at the proposed position.
	 */
	private boolean testTilemapCollision(PhysicsColliderNode entityNode,
										 TilemapColliderNode tilemapNode,
										 Vector2D proposedPos) {
		// Get tilemap properties
		TilemapComponent tilemap = tilemapNode.tilemap;
		TilemapColliderComponent tilemapCollider = tilemapNode.tilemapCollider;
		Vector2D tilemapPos = tilemapNode.transform.getPosition();
		int tileSize = tilemap.getTileSize();

		// Get entity collider properties
		ColliderComponent collider = entityNode.collider;

		// For a CircleShape, we need to check surrounding tiles
		if (collider.getCollisionShape() instanceof CircleShape) {
			CircleShape circleShape = (CircleShape) collider.getCollisionShape();
			float radius = circleShape.getRadius();

			// Add a small buffer to the radius for more reliable detection
			float effectiveRadius = radius * 1.01f; // 1% buffer for edge detection

			// Calculate world position (entity position + collider offset)
			Vector2D worldPos = proposedPos.add(collider.getOffset());

			// Calculate tilemap relative position
			Vector2D relativePos = worldPos.subtract(tilemapPos);

			// Find the tile coordinates using float division then convert to int
			float exactTileX = relativePos.getX() / tileSize;
			float exactTileY = relativePos.getY() / tileSize;
			int centerTileX = (int)Math.floor(exactTileX);
			int centerTileY = (int)Math.floor(exactTileY);

			// Calculate how many tiles to check based on radius
			int tilesCheck = (int)Math.ceil(effectiveRadius / tileSize) + 1;

			// Limit check range to what is relevant
			int startX = Math.max(0, centerTileX - tilesCheck);
			int endX = Math.min(tilemapCollider.getWidth() - 1, centerTileX + tilesCheck);
			int startY = Math.max(0, centerTileY - tilesCheck);
			int endY = Math.min(tilemapCollider.getHeight() - 1, centerTileY + tilesCheck);

			// Check surrounding tiles
			for (int y = startY; y <= endY; y++) {
				for (int x = startX; x <= endX; x++) {
					// Create tile coordinate pair
					Pair<Integer, Integer> tileCoord = Pair.of(x, y);

					// Check if we know this tile is not solid (safe to cache)
					Boolean isNonSolid = nonCollidingTileCache.get(tileCoord);
					if (Boolean.TRUE.equals(isNonSolid)) {
						continue; // Skip non-solid tiles
					}

					// For all other tiles, we need to check if they're solid
					if (!tilemapCollider.isSolid(x, y)) {
						nonCollidingTileCache.put(tileCoord, true);
						continue;
					}

					// For solid tiles, always perform the full collision check
					Vector2D tilePos = tilemapPos.add(new Vector2D(
						x * tileSize,
						y * tileSize
					));

					// Create tile shape
					RectangleShape tileShape = new RectangleShape(
						tilePos,
						tileSize,
						tileSize
					);

					// Check for collision with proposed position
					CircleShape proposedCircle = new CircleShape(worldPos, effectiveRadius);

					// Test for intersection
					if (testCircleRectIntersection(proposedCircle, tileShape)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Tests for intersection between a circle and rectangle with consistent edge detection.
	 */
	private boolean testCircleRectIntersection(CircleShape circle, RectangleShape rect) {
		Vector2D center = circle.getCenter();
		float radius = circle.getRadius();

		// Get rectangle position and dimensions
		Vector2D rectPos = rect.getPosition();
		float rectWidth = rect.getWidth();
		float rectHeight = rect.getHeight();

		// Calculate rectangle edges with a small threshold for more consistent detection
		float leftEdge = rectPos.getX() - COLLISION_THRESHOLD;
		float rightEdge = rectPos.getX() + rectWidth + COLLISION_THRESHOLD;
		float topEdge = rectPos.getY() - COLLISION_THRESHOLD;
		float bottomEdge = rectPos.getY() + rectHeight + COLLISION_THRESHOLD;

		// Find the closest point on the buffered rectangle to the circle's center
		float closestX = Math.max(leftEdge, Math.min(center.getX(), rightEdge));
		float closestY = Math.max(topEdge, Math.min(center.getY(), bottomEdge));

		// Calculate distance between closest point and circle center
		float distanceX = closestX - center.getX();
		float distanceY = closestY - center.getY();
		float distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);

		// If distance is less than radius squared, shapes intersect
		return distanceSquared <= (radius * radius);
	}

	/**
	 * Cleans up any collision pairs involving the given entity.
	 * Called automatically when an entity is removed from a scene.
	 */
	public void cleanupEntity(Entity entity) {
		if (entity == null) {
			return;
		}

		synchronized(collisionLock) {
			// Remove any pairs that involve this entity
			activeCollisions.removeIf(pair ->
				pair.getFirst().equals(entity) || pair.getSecond().equals(entity));
			previousFrameCollisions.removeIf(pair ->
				pair.getFirst().equals(entity) || pair.getSecond().equals(entity));
		}
	}

	@Override
	public boolean checkCollision(ICollider a, ICollider b) {
		if (a == null || b == null || a.getCollisionShape() == null || b.getCollisionShape() == null) {
			return false;
		}

		// Special case for CircleShape vs CircleShape for more accurate world position handling
		if (a.getCollisionShape() instanceof CircleShape && b.getCollisionShape() instanceof CircleShape) {
			CircleShape circleA = (CircleShape) a.getCollisionShape();
			CircleShape circleB = (CircleShape) b.getCollisionShape();

			// For ColliderComponents, get world position using entity position + offset
			Vector2D worldPosA = circleA.getCenter();
			Vector2D worldPosB = circleB.getCenter();

			if (a instanceof ColliderComponent && a.getEntity() != null) {
				Entity entityA = a.getEntity();
				TransformComponent transformA = entityA.getComponent(TransformComponent.class);
				if (transformA != null) {
					worldPosA = transformA.getPosition().add(((ColliderComponent) a).getOffset());
				}
			}

			if (b instanceof ColliderComponent && b.getEntity() != null) {
				Entity entityB = b.getEntity();
				TransformComponent transformB = entityB.getComponent(TransformComponent.class);
				if (transformB != null) {
					worldPosB = transformB.getPosition().add(((ColliderComponent) b).getOffset());
				}
			}

			// Create temporary circles with correct world positions
			CircleShape worldCircleA = new CircleShape(worldPosA, circleA.getRadius());
			CircleShape worldCircleB = new CircleShape(worldPosB, circleB.getRadius());

			// Check intersection with accurate world positions
			return worldCircleA.intersects(worldCircleB);
		}

		// Default to basic shape intersection test
		return a.getCollisionShape().intersects(b.getCollisionShape());
	}

	@Override
	public boolean checkTileCollision(ICollider collider, int tileX, int tileY, int tileSize) {
		if (collider == null || collider.getCollisionShape() == null) {
			return false;
		}

		// Create a rectangle shape for the tile
		RectangleShape tileShape = new RectangleShape(
			new Vector2D(tileX * tileSize, tileY * tileSize),
			tileSize,
			tileSize
		);

		// Special case for circle colliders with world position handling
		if (collider.getCollisionShape() instanceof CircleShape && collider instanceof ColliderComponent) {
			CircleShape circle = (CircleShape) collider.getCollisionShape();
			Entity entity = collider.getEntity();

			if (entity != null) {
				TransformComponent transform = entity.getComponent(TransformComponent.class);
				if (transform != null) {
					// Get world position (entity position + collider offset)
					Vector2D worldPos = transform.getPosition().add(((ColliderComponent)collider).getOffset());

					// Create circle at correct world position
					CircleShape worldCircle = new CircleShape(worldPos, circle.getRadius());

					// Test intersection with tile
					return testCircleRectIntersection(worldCircle, tileShape);
				}
			}
		}

		// Default to basic intersection test
		return collider.getCollisionShape().intersects(tileShape);
	}

	@Override
	public boolean isPositionValid(ICollider collider, Vector2D proposedPosition) {
		// Skip if collider is invalid
		if (collider == null || collider.getCollisionShape() == null) {
			return true;
		}

		// Get all tilemap colliders
		Set<TilemapColliderNode> tilemapNodes = NodeManager.active().getNodes(TilemapColliderNode.class);

		// If no tilemaps, position is valid
		if (tilemapNodes.isEmpty()) {
			return true;
		}

		// For circle colliders, use our optimized spatial method
		if (collider.getCollisionShape() instanceof CircleShape && collider instanceof ColliderComponent) {
			for (TilemapColliderNode tilemapNode : tilemapNodes) {
				// Skip invalid nodes
				if (tilemapNode == null || tilemapNode.transform == null ||
					tilemapNode.tilemap == null || tilemapNode.tilemapCollider == null) {
					continue;
				}

				// Create a dummy physics node to test against the tilemap
				PhysicsColliderNode dummyNode = new PhysicsColliderNode();
				dummyNode.transform = new TransformComponent(proposedPosition, 0, new Vector2D(1, 1));
				dummyNode.collider = (ColliderComponent)collider;

				// Test for collision with the tilemap
				if (testTilemapCollision(dummyNode, tilemapNode, proposedPosition)) {
					return false;
				}
			}

			return true;
		}

		// For other collider types, use a more general approach
		for (TilemapColliderNode tilemapNode : tilemapNodes) {
			TilemapComponent tilemap = tilemapNode.tilemap;
			TilemapColliderComponent tilemapCollider = tilemapNode.tilemapCollider;
			Vector2D tilemapPos = tilemapNode.transform.getPosition();
			int tileSize = tilemap.getTileSize();

			// Create an AABB for the collider
			AABB colliderAABB = getColliderAABB(collider, proposedPosition);

			// Get tile coordinates for the collider AABB
			int minTileX = Math.max(0, (int)Math.floor((colliderAABB.getMinX() - tilemapPos.getX()) / tileSize));
			int maxTileX = Math.min(tilemapCollider.getWidth() - 1,
				(int)Math.ceil((colliderAABB.getMaxX() - tilemapPos.getX()) / tileSize));
			int minTileY = Math.max(0, (int)Math.floor((colliderAABB.getMinY() - tilemapPos.getY()) / tileSize));
			int maxTileY = Math.min(tilemapCollider.getHeight() - 1,
				(int)Math.ceil((colliderAABB.getMaxY() - tilemapPos.getY()) / tileSize));

			// Check all tiles in the collider's AABB
			for (int y = minTileY; y <= maxTileY; y++) {
				for (int x = minTileX; x <= maxTileX; x++) {
					if (tilemapCollider.isSolid(x, y)) {
						// Convert tile to world position
						Vector2D tilePos = tilemapPos.add(new Vector2D(x * tileSize, y * tileSize));

						// Create tile shape
						RectangleShape tileShape = new RectangleShape(tilePos, tileSize, tileSize);

						// Check for intersection with the collider
						if (collider.getCollisionShape().intersects(tileShape)) {
							return false;
						}
					}
				}
			}
		}

		return true;
	}

	/**
	 * Gets an AABB for a collider at the given position.
	 */
	private AABB getColliderAABB(ICollider collider, Vector2D position) {
		ICollisionShape shape = collider.getCollisionShape();
		Vector2D offset = new Vector2D(0, 0);

		// Add collider offset if available
		if (collider instanceof ColliderComponent) {
			offset = ((ColliderComponent)collider).getOffset();
		}

		// Position with offset applied
		Vector2D worldPos = position.add(offset);

		if (shape instanceof CircleShape) {
			CircleShape circle = (CircleShape)shape;
			float radius = circle.getRadius();
			return new AABB(
				worldPos.getX() - radius,
				worldPos.getY() - radius,
				worldPos.getX() + radius,
				worldPos.getY() + radius
			);
		}
		else if (shape instanceof RectangleShape) {
			RectangleShape rect = (RectangleShape)shape;
			return new AABB(
				rect.getPosition().getX(),
				rect.getPosition().getY(),
				rect.getPosition().getX() + rect.getWidth(),
				rect.getPosition().getY() + rect.getHeight()
			);
		}

		// fallback for other shape types
		return new AABB(
			worldPos.getX() - 1.0f,
			worldPos.getY() - 1.0f,
			worldPos.getX() + 1.0f,
			worldPos.getY() + 1.0f
		);
	}

	/**
	 * Gets the layer collision matrix.
	 */
	public LayerCollisionMatrix getLayerMatrix() {
		return layerMatrix;
	}
}