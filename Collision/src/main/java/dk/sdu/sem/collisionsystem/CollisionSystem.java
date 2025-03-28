package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.*;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.components.TilemapColliderComponent;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.collision.shapes.RectangleShape;
import dk.sdu.sem.commonsystem.*;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.TilemapComponent;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;
import dk.sdu.sem.gamesystem.services.IStart;

import java.util.*;
import java.util.ServiceLoader;

/**
 * System that handles collision detection and resolution.
 */
public class CollisionSystem implements ICollisionSPI, IFixedUpdate, IStart {
	// Cache for non-colliding tiles to improve performance
	private final Map<Pair<Integer, Integer>, Boolean> nonCollidingTileCache = new HashMap<>();

	// Small threshold for consistent edge detection
	private static final float COLLISION_THRESHOLD = 0.03f;

	// Debug flag - set to true to enable logging
	private static final boolean DEBUG_MODE = false;

	// Trigger-related fields
	private final List<ITriggerCollisionListener> triggerListeners = new ArrayList<>();
	private Set<Pair<Entity, Entity>> activeCollisions = new HashSet<>();
	private Set<Pair<Entity, Entity>> previousFrameCollisions = new HashSet<>();

	public CollisionSystem() {
		System.out.println("CollisionSystem initialized");
	}

	@Override
	public void start() {
		// Find and load trigger listeners via ServiceLoader
		ServiceLoader.load(ITriggerCollisionListener.class).forEach(triggerListeners::add);
		System.out.println("CollisionSystem loaded " + triggerListeners.size() + " trigger listeners");
	}

	/**
	 * Registers a listener to receive trigger collision notifications.
	 * Should be called by systems that need to track trigger events.
	 */
	public void registerTriggerListener(ITriggerCollisionListener listener) {
		if (!triggerListeners.contains(listener)) {
			triggerListeners.add(listener);
			System.out.println("Registered trigger listener: " + listener.getClass().getName());
		}
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
			float deltaTime = (float)Time.getFixedDeltaTime();

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
					if (DEBUG_MODE) {
						System.out.println("X collision detected at " + proposedXPos);
					}
					break;
				}
			}

			// Try Y movement from the updated position
			Vector2D testPos = xCollision ? currentPos : proposedXPos;
			Vector2D proposedYPos = testPos.add(yMovement);
			for (TilemapColliderNode tilemapNode : tilemapNodes) {
				if (testTilemapCollision(physicsNode, tilemapNode, proposedYPos)) {
					yCollision = true;
					if (DEBUG_MODE) {
						System.out.println("Y collision detected at " + proposedYPos);
					}
					break;
				}
			}

			// Now we only modify velocity to account for collisions :D
			Vector2D newVelocity = velocity;
			if (xCollision) {
				newVelocity = new Vector2D(0, newVelocity.getY());
			}
			if (yCollision) {
				newVelocity = new Vector2D(newVelocity.getX(), 0);
			}

			physicsNode.physicsComponent.setVelocity(newVelocity);

			// We could store collision info in the physics component if needed
			if (xCollision || yCollision) {
				// Might not be needed
				// physicsNode.physicsComponent.setCollisionFlags(xCollision, yCollision);
			}
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

		// Convert to list for indexed access
		List<ColliderNode> colliderList = new ArrayList<>(colliderNodes);

		// Check each pair (only check each pair once)
		for (int i = 0; i < colliderList.size() - 1; i++) {
			ColliderNode nodeA = colliderList.get(i);
			Entity entityA = nodeA.getEntity();
			ColliderComponent colliderA = nodeA.collider;

			for (int j = i + 1; j < colliderList.size(); j++) {
				ColliderNode nodeB = colliderList.get(j);
				Entity entityB = nodeB.getEntity();
				ColliderComponent colliderB = nodeB.collider;

				// Skip if both colliders are triggers (triggers don't collide with each other)
				if (colliderA.isTrigger() && colliderB.isTrigger()) {
					continue;
				}

				// Check for collision using the shape intersection test
				boolean collides = colliderA.getCollisionShape().intersects(colliderB.getCollisionShape());

				if (collides) {
					// Record this collision
					Pair<Entity, Entity> pair = Pair.ordered(entityA, entityB);
					activeCollisions.add(pair);

					// Check if this is the first frame of collision
					boolean isCollisionStart = !previousFrameCollisions.contains(pair);

					// Handle trigger collision if either collider is a trigger
					if (colliderA.isTrigger()) {
						notifyTriggerCollision(entityA, entityB, isCollisionStart);
					}
					if (colliderB.isTrigger()) {
						notifyTriggerCollision(entityB, entityA, isCollisionStart);
					}

					// For regular collisions (neither is a trigger), we handle this in resolveCollisions method
				}
			}
		}
	}

	/**
	 * Tests if an entity would collide with a tilemap at the proposed position.
	 * Uses spatial partitioning principles to optimize performance.
	 *
	 * @param entityNode Entity to test
	 * @param tilemapNode Tilemap to test against
	 * @param proposedPos Proposed position to test
	 * @return true if collision would occur, false otherwise
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

			if (DEBUG_MODE) {
				System.out.printf("Circle: (%.2f, %.2f), Radius: %.2f, Exact tile: (%.2f, %.2f), Tile: (%d, %d)\n",
					worldPos.getX(), worldPos.getY(), radius, exactTileX, exactTileY, centerTileX, centerTileY);
			}

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
						if (DEBUG_MODE) {
							System.out.printf("Collision with tile [%d, %d] at pos (%.2f, %.2f)\n",
								x, y, tilePos.getX(), tilePos.getY());
						}
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Tests for intersection between a circle and rectangle with consistent edge detection.
	 *
	 * @param circle The circle shape
	 * @param rect The rectangle shape
	 * @return true if the shapes intersect, false otherwise
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
	 */
	private void notifyTriggerCollision(Entity triggerEntity, Entity otherEntity, boolean isCollisionStart) {
		for (ITriggerCollisionListener listener : triggerListeners) {
			listener.onTriggerCollision(triggerEntity, otherEntity, isCollisionStart);
		}
	}

	/**
	 * Notifies all trigger listeners about a collision exit.
	 */
	private void notifyTriggerCollisionEnd(Entity triggerEntity, Entity otherEntity) {
		for (ITriggerCollisionListener listener : triggerListeners) {
			listener.onTriggerCollisionEnd(triggerEntity, otherEntity);
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
}