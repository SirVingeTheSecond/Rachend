package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.ICollider;
import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.ITriggerCollisionListener;
import dk.sdu.sem.collision.PhysicsLayer;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.components.TilemapColliderComponent;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.collision.shapes.RectangleShape;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Pair;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.TilemapComponent;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;
import dk.sdu.sem.gamesystem.services.IStart;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System that handles collision detection and resolution.
 */
public class CollisionSystem implements ICollisionSPI, IFixedUpdate, IStart {
	private static final Logger LOGGER = Logger.getLogger(CollisionSystem.class.getName());

	// Cache for non-colliding tiles to improve performance
	private final Map<Pair<Integer, Integer>, Boolean> nonCollidingTileCache = new HashMap<>();

	// Small threshold for consistent edge detection
	private static final float COLLISION_THRESHOLD = 0.03f;

	// Configuration
	private static final boolean DEBUG = false;

	// Trigger-related fields
	private final List<ITriggerCollisionListener> triggerListeners = new ArrayList<>();
	private Set<Pair<Entity, Entity>> activeCollisions = new HashSet<>();
	private Set<Pair<Entity, Entity>> previousFrameCollisions = new HashSet<>();

	// Physics layer matrix for collision filtering
	private final LayerCollisionMatrix layerMatrix = new LayerCollisionMatrix();

	// Static reference to the active instance
	private static CollisionSystem instance;

	public CollisionSystem() {
		LOGGER.info("CollisionSystem initialized");
		instance = this; // Store reference to self
	}

	/**
	 * Get the active CollisionSystem instance.
	 */
	public static CollisionSystem getInstance() {
		return instance;
	}

	@Override
	public void start() {
		// Find and load trigger listeners via ServiceLoader
		ServiceLoader.load(ITriggerCollisionListener.class).forEach(triggerListeners::add);
		LOGGER.info("CollisionSystem loaded " + triggerListeners.size() + " trigger listeners");

		// Ensure player can collide with items
		layerMatrix.enableLayerCollision(PhysicsLayer.PLAYER, PhysicsLayer.ITEM);
	}

	/**
	 * Registers a listener to receive trigger collision notifications.
	 * Should be called by systems that need to track trigger events.
	 *
	 * @param listener The listener to register
	 * @return True if the listener was registered, false if it was already registered
	 */
	public boolean registerTriggerListener(ITriggerCollisionListener listener) {
		if (listener == null) {
			LOGGER.warning("Cannot register null listener");
			return false;
		}

		if (triggerListeners.contains(listener)) {
			if (DEBUG) LOGGER.info("Listener already registered: " + listener.getClass().getName());
			return false;
		}

		boolean added = triggerListeners.add(listener);
		if (added) {
			if (DEBUG) {
				LOGGER.info("Successfully registered listener: " + listener.getClass().getName());
				LOGGER.info("Current listener count: " + triggerListeners.size());
			}
		} else {
			LOGGER.warning("Failed to register listener: " + listener.getClass().getName());
		}

		return added;
	}

	@Override
	public void fixedUpdate() {
		// Swap collision sets to track new vs. continued collisions
		Set<Pair<Entity, Entity>> tmp = previousFrameCollisions;
		previousFrameCollisions = activeCollisions;
		activeCollisions = tmp;
		activeCollisions.clear();

		// Process all entity-tilemap collisions
		resolveCollisions();

		// Process entity-to-entity collisions for triggers
		detectEntityCollisions();

		// Find collision exits (pairs in previousFrameCollisions but not in activeCollisions)
		handleCollisionExits();
	}

	/**
	 * Handle collisions that are no longer active
	 */
	private void handleCollisionExits() {
		for (Pair<Entity, Entity> pair : previousFrameCollisions) {
			if (!activeCollisions.contains(pair)) {
				// This is a collision exit - find which is the trigger
				Entity entity1 = pair.getFirst();
				Entity entity2 = pair.getSecond();

				ColliderComponent collider1 = entity1.getComponent(ColliderComponent.class);
				ColliderComponent collider2 = entity2.getComponent(ColliderComponent.class);

				if (collider1 != null && collider1.isTrigger()) {
					notifyTriggerCollisionEnd(entity1, entity2);
				} else if (collider2 != null && collider2.isTrigger()) {
					notifyTriggerCollisionEnd(entity2, entity1);
				}
			}
		}
	}

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
	 * Detects collisions between entities with colliders.
	 * Handles both trigger and non-trigger collisions.
	 */
	private void detectEntityCollisions() {
		// Get all collider nodes
		Set<ColliderNode> colliderNodes = NodeManager.active().getNodes(ColliderNode.class);

		// Skip processing if there are fewer than 2 colliders
		if (colliderNodes.size() < 2) {
			return;
		}

		// Convert to list for indexed access and make a copy to prevent concurrent modification
		List<ColliderNode> colliderList = new ArrayList<>(colliderNodes);

		// Check each pair (only check each pair once)
		for (int i = 0; i < colliderList.size() - 1; i++) {
			ColliderNode nodeA = colliderList.get(i);
			Entity entityA = nodeA.getEntity();

			// Skip if the entity is null or has been removed from the scene
			if (entityA == null || entityA.getScene() == null) {
				continue;
			}

			ColliderComponent colliderA = nodeA.collider;
			if (colliderA == null) continue;

			for (int j = i + 1; j < colliderList.size(); j++) {
				ColliderNode nodeB = colliderList.get(j);
				Entity entityB = nodeB.getEntity();

				// Skip if the entity is null or has been removed from the scene
				if (entityB == null || entityB.getScene() == null) {
					continue;
				}

				ColliderComponent colliderB = nodeB.collider;
				if (colliderB == null) continue;

				if (DEBUG) {
					LOGGER.fine("Checking collision between:");
					LOGGER.fine("- Entity A: " + entityA.getID() +
						" (isTrigger: " + colliderA.isTrigger() +
						", Layer: " + colliderA.getLayer() + ")");
					LOGGER.fine("- Entity B: " + entityB.getID() +
						" (isTrigger: " + colliderB.isTrigger() +
						", Layer: " + colliderB.getLayer() + ")");
				}

				// Check if these layers can collide using the matrix
				PhysicsLayer layerA = colliderA.getLayer();
				PhysicsLayer layerB = colliderB.getLayer();

				if (!layerMatrix.canLayersCollide(layerA, layerB)) {
					continue;
				}

				// Skip item-item collision notifications - prevents excessive trigger events
				if (layerA == PhysicsLayer.ITEM && layerB == PhysicsLayer.ITEM) {
					continue;
				}

				// Check for collision using the shape intersection test with proper world positions
				boolean collides = false;
				try {
					// For CircleShape-CircleShape collision, use world positions
					if (colliderA.getCollisionShape() instanceof CircleShape &&
						colliderB.getCollisionShape() instanceof CircleShape) {

						CircleShape circleA = (CircleShape) colliderA.getCollisionShape();
						CircleShape circleB = (CircleShape) colliderB.getCollisionShape();

						// Calculate world positions by adding entity position to collider offset
						Vector2D worldPosA = nodeA.transform.getPosition().add(colliderA.getOffset());
						Vector2D worldPosB = nodeB.transform.getPosition().add(colliderB.getOffset());

						// Create temporary circle shapes with correct world positions
						CircleShape worldCircleA = circleA.withCenter(worldPosA);
						CircleShape worldCircleB = circleB.withCenter(worldPosB);

						// Check if the world-positioned shapes intersect
						collides = worldCircleA.intersects(worldCircleB);
					}
					else {
						// For other shape combinations, use default method
						// NOTE: This may not handle world positions correctly
						collides = colliderA.getCollisionShape().intersects(colliderB.getCollisionShape());
					}
				} catch (Exception e) {
					LOGGER.log(Level.WARNING, "Error checking collision: " + e.getMessage(), e);
					continue;
				}

				if (collides) {
					handleCollision(entityA, entityB, colliderA, colliderB);
				}
			}
		}
	}

	/**
	 * Handles a detected collision between two entities
	 */
	private void handleCollision(Entity entityA, Entity entityB,
								 ColliderComponent colliderA, ColliderComponent colliderB) {
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

		// DIRECT APPROACH: Handle player-item collisions immediately via direct notification
		// This bypasses the listener system to ensure item pickups work
		boolean playerItemCollision = checkForPlayerItemCollision(entityA, entityB, isCollisionStart);

		// Handle trigger collision if either collider is a trigger - still support this for other triggers
		if (colliderA.isTrigger() && !playerItemCollision) {
			try {
				notifyTriggerCollision(entityA, entityB, isCollisionStart);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Error notifying trigger collision: " + e.getMessage(), e);
			}
		}

		if (colliderB.isTrigger() && !playerItemCollision) {
			try {
				notifyTriggerCollision(entityB, entityA, isCollisionStart);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Error notifying trigger collision: " + e.getMessage(), e);
			}
		}
	}

	/**
	 * Checks for and handles player-item collisions directly for optimization
	 * @return true if a player-item collision was handled
	 */
	private boolean checkForPlayerItemCollision(Entity entityA, Entity entityB, boolean isCollisionStart) {
		boolean playerItemCollision = false;
		Entity playerEntity = null;
		Entity itemEntity = null;

		// Check if this is a player-item collision (either way)
		if (entityA.hasComponent(dk.sdu.sem.player.PlayerComponent.class) &&
			entityB.hasComponent(dk.sdu.sem.commonitem.ItemComponent.class)) {
			playerEntity = entityA;
			itemEntity = entityB;
			playerItemCollision = true;
		} else if (entityB.hasComponent(dk.sdu.sem.player.PlayerComponent.class) &&
			entityA.hasComponent(dk.sdu.sem.commonitem.ItemComponent.class)) {
			playerEntity = entityB;
			itemEntity = entityA;
			playerItemCollision = true;
		}

		// Only process player-item collisions on the first frame
		if (playerItemCollision && isCollisionStart) {
			if (DEBUG) LOGGER.info("Detected player-item collision, notifying directly");
			ServiceLoader<dk.sdu.sem.collision.ITriggerEventSPI> handlers =
				ServiceLoader.load(dk.sdu.sem.collision.ITriggerEventSPI.class);

			for (dk.sdu.sem.collision.ITriggerEventSPI handler : handlers) {
				try {
					if (DEBUG) LOGGER.info("Notifying handler directly: " + handler.getClass().getName());
					handler.processTriggerEvent(
						dk.sdu.sem.collision.ITriggerEventSPI.TriggerEventType.ENTER,
						itemEntity,
						playerEntity
					);
				} catch (Exception e) {
					LOGGER.log(Level.WARNING, "Error in direct handler notification: " + e.getMessage(), e);
				}
			}
		}

		return playerItemCollision;
	}

	/**
	 * Tests if an entity would collide with a tilemap at the proposed position.
	 * Uses spatial partitioning principles to optimize performance.
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
	 * Notifies all trigger listeners about a collision.
	 * This method should be called whenever a trigger collision is detected.
	 */
	private void notifyTriggerCollision(Entity triggerEntity, Entity otherEntity, boolean isCollisionStart) {
		if (DEBUG) {
			LOGGER.fine("notifyTriggerCollision called");
			LOGGER.fine("triggerListeners.size() = " + triggerListeners.size());
		}

		// Always attempt to get the TriggerSystem directly if needed
		if (triggerListeners.isEmpty()) {
			if (DEBUG) LOGGER.info("No listeners registered, trying to find TriggerSystem directly");
			// Try to find TriggerSystem using ServiceLoader as a last resort
			ServiceLoader<ITriggerCollisionListener> listeners = ServiceLoader.load(ITriggerCollisionListener.class);
			for (ITriggerCollisionListener listener : listeners) {
				if (listener instanceof TriggerSystem) {
					if (DEBUG) LOGGER.info("Found TriggerSystem via ServiceLoader, notifying directly");
					listener.onTriggerCollision(triggerEntity, otherEntity, isCollisionStart);
					return;
				}
			}
			LOGGER.warning("No trigger listeners registered or found, skipping notification");
			return;
		}

		if (DEBUG) {
			LOGGER.info("Notifying " + triggerListeners.size() +
				" listeners of collision between " + triggerEntity.getID() +
				" and " + otherEntity.getID());
		}

		for (ITriggerCollisionListener listener : triggerListeners) {
			try {
				if (DEBUG) LOGGER.fine("Notifying listener: " + listener.getClass().getName());
				listener.onTriggerCollision(triggerEntity, otherEntity, isCollisionStart);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Error in listener: " + e.getMessage(), e);
			}
		}
	}

	/**
	 * Notifies all trigger listeners about a collision exit.
	 * This method should be called whenever a trigger collision ends.
	 */
	private void notifyTriggerCollisionEnd(Entity triggerEntity, Entity otherEntity) {
		if (triggerListeners.isEmpty()) {
			return;
		}

		if (DEBUG) {
			LOGGER.info("Notifying " + triggerListeners.size() +
				" listeners of collision end between " + triggerEntity.getID() +
				" and " + otherEntity.getID());
		}

		for (ITriggerCollisionListener listener : triggerListeners) {
			try {
				listener.onTriggerCollisionEnd(triggerEntity, otherEntity);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Error in listener: " + e.getMessage(), e);
			}
		}
	}

	@Override
	public boolean checkCollision(ICollider a, ICollider b) {
		return a.getCollisionShape().intersects(b.getCollisionShape());
	}

	@Override
	public boolean checkTileCollision(ICollider collider, int tileX, int tileY, int tileSize) {
		RectangleShape tileShape = new RectangleShape(
			new Vector2D(tileX * tileSize, tileY * tileSize),
			tileSize,
			tileSize
		);

		return collider.getCollisionShape().intersects(tileShape);
	}

	@Override
	public boolean isPositionValid(ICollider collider, Vector2D proposedPosition) {
		// Get all tilemap colliders
		Set<TilemapColliderNode> tilemapNodes = NodeManager.active().getNodes(TilemapColliderNode.class);

		// If no tilemaps, position is valid
		if (tilemapNodes.isEmpty()) {
			return true;
		}

		// Check against all tilemaps
		for (TilemapColliderNode tilemapNode : tilemapNodes) {
			// Get tilemap properties
			TilemapComponent tilemap = tilemapNode.tilemap;
			TilemapColliderComponent tilemapCollider = tilemapNode.tilemapCollider;
			Vector2D tilemapPos = tilemapNode.transform.getPosition();
			int tileSize = tilemap.getTileSize();

			// We need to check surrounding tiles
			if (collider.getCollisionShape() instanceof CircleShape) {
				CircleShape circleShape = (CircleShape) collider.getCollisionShape();
				float radius = circleShape.getRadius();
				// Add small buffer
				float effectiveRadius = radius * 1.01f;

				// Calculate world position
				Vector2D worldPos = proposedPosition;
				if (collider instanceof ColliderComponent) {
					worldPos = proposedPosition.add(((ColliderComponent)collider).getOffset());
				}

				// Calculate tilemap relative position
				Vector2D relativePos = worldPos.subtract(tilemapPos);

				// Find the tile coordinates
				float exactTileX = relativePos.getX() / tileSize;
				float exactTileY = relativePos.getY() / tileSize;
				int centerTileX = (int)Math.floor(exactTileX);
				int centerTileY = (int)Math.floor(exactTileY);

				// Calculate how many tiles to check based on radius
				int tilesCheck = (int)Math.ceil(effectiveRadius / tileSize) + 1;

				// Limit check range
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

						// For all other tiles, we need to check if they are solid
						if (!tilemapCollider.isSolid(x, y)) {
							nonCollidingTileCache.put(tileCoord, true);
							continue;
						}

						// For solid tiles, always perform the full collision check!
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
						if (testCircleRectIntersection(proposedCircle, tileShape)) {
							return false;
						}
					}
				}
			}
		}

		return true;
	}

	/**
	 * Cleans up any collision pairs involving the given entity.
	 * Should be called when an entity is removed from a scene.
	 */
	public void cleanupEntity(Entity entity) {
		// Remove any pairs that involve this entity
		activeCollisions.removeIf(pair ->
			pair.getFirst() == entity || pair.getSecond() == entity);
		previousFrameCollisions.removeIf(pair ->
			pair.getFirst() == entity || pair.getSecond() == entity);
	}

	/**
	 * Gets the layer collision matrix
	 */
	public LayerCollisionMatrix getLayerMatrix() {
		return layerMatrix;
	}
}