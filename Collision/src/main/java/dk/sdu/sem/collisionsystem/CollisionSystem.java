package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.*;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Pair;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.TilemapComponent;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * System that handles collision detection and resolution.
 * <p>
 * At some point, this could be split into CollisionDetector and CollisionResolver which would be used by this system.
 */
public class CollisionSystem implements ICollisionSPI, IFixedUpdate {
	// Cache only non-solid tile checks for performance
	private final Map<Pair<Integer, Integer>, Boolean> nonSolidTileCache = new HashMap<>();

	@Override
	public void fixedUpdate() {
		// Process all entity-tilemap collisions
		handleEntityTilemapCollisions();
	}

	/**
	 * Processes collisions between entities and tilemaps.
	 * Only checks entities that are actually moving
	 */
	private void handleEntityTilemapCollisions() {
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

			// Calculate proposed position after movement
			Vector2D currentPos = physicsNode.transform.getPosition();
			Vector2D velocity = physicsNode.physicsComponent.getVelocity();
			Vector2D proposedPos = currentPos.add(
				velocity.scale((float)Time.getFixedDeltaTime())
			);

			// Check against all tilemaps
			boolean collision = false;
			for (TilemapColliderNode tilemapNode : tilemapNodes) {
				if (wouldCollideWithTilemap(physicsNode, tilemapNode, proposedPos)) {
					collision = true;
					break;
				}
			}

			// If collision detected, zero out velocity.
			if (collision) {
				physicsNode.physicsComponent.setVelocity(new Vector2D(0, 0));
			}
		}
	}

	/**
	 * Collision check trying to follow the principles of spatial partitioning.
	 * Only checks tiles that could possibly intersect with the entity.
	 * Only caches non-solid tile checks for safety.
	 */
	private boolean wouldCollideWithTilemap(PhysicsColliderNode entityNode,
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

			// Calculate world position (entity position + collider offset)
			Vector2D worldPos = proposedPos.add(collider.getOffset());

			// Calculate tilemap relative position
			Vector2D relativePos = worldPos.subtract(tilemapPos);

			// Find the tile coordinates - optimization: use integer division
			int centerTileX = (int)(relativePos.getX() / tileSize);
			int centerTileY = (int)(relativePos.getY() / tileSize);

			// Calculate how many tiles to check based on radius
			// +1 to ensure we check enough tiles, ceil to avoid missing edge cases
			int tilesCheck = (int)Math.ceil(radius / tileSize) + 1;

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
					Boolean isNonSolid = nonSolidTileCache.get(tileCoord);
					if (Boolean.TRUE.equals(isNonSolid)) {
						continue; // Skip non-solid tiles
					}

					// For all other tiles, we need to check if they're solid
					if (!tilemapCollider.isSolid(x, y)) {
						nonSolidTileCache.put(tileCoord, true);
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
					CircleShape proposedCircle = new CircleShape(worldPos, radius);
					if (proposedCircle.intersects(tileShape)) {
						return true;
					}
				}
			}
		}

		return false;
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
			if (collider.getCollisionShape() instanceof CircleShape circleShape) {
				float radius = circleShape.getRadius();

				// Calculate world position
				Vector2D worldPos = proposedPosition;
				if (collider instanceof ColliderComponent) {
					worldPos = proposedPosition.add(((ColliderComponent)collider).getOffset());
				}

				// Calculate tilemap relative position
				Vector2D relativePos = worldPos.subtract(tilemapPos);

				// Find the tile coordinates
				int centerTileX = (int)(relativePos.getX() / tileSize);
				int centerTileY = (int)(relativePos.getY() / tileSize);

				// Calculate how many tiles to check based on radius
				int tilesCheck = (int)Math.ceil(radius / tileSize) + 1;

				// Again, limit check range
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
						Boolean isNonSolid = nonSolidTileCache.get(tileCoord);
						if (Boolean.TRUE.equals(isNonSolid)) {
							continue; // Skip non-solid tiles
						}

						// For all other tiles, we need to check if they are solid
						if (!tilemapCollider.isSolid(x, y)) {
							nonSolidTileCache.put(tileCoord, true);
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
						CircleShape proposedCircle = new CircleShape(worldPos, radius);
						if (proposedCircle.intersects(tileShape)) {
							return false;
						}
					}
				}
			}
		}

		return true;
	}
}