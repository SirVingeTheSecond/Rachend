package dk.sdu.sem.collisionsystem.detection;

import dk.sdu.sem.collision.ICollider;
import dk.sdu.sem.collision.PhysicsLayer;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.collision.shapes.RectangleShape;
import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.collisionsystem.*;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.collisionsystem.broadphase.BroadphaseStrategy;
import dk.sdu.sem.collisionsystem.broadphase.QuadTreeBroadphase;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles all collision detection operations.
 * Includes both broadphase and narrowphase collision checking.
 */
public class CollisionDetector {
	private static final float COLLISION_THRESHOLD = 0.03f; // Skin-width
	private final LayerCollisionMatrix layerMatrix = new LayerCollisionMatrix();

	private final BroadphaseStrategy broadphase;

	public CollisionDetector() {
		// Default to quadtree broadphase - could be made configurable
		this.broadphase = new QuadTreeBroadphase();
	}

	/**
	 * Detects all collisions between a set of collider nodes.
	 *
	 * @param colliderNodes Set of collider nodes to check
	 * @return Set of collision pairs that were detected
	 */
	public Set<CollisionPair> detectCollisions(Set<ColliderNode> colliderNodes) {
		Set<CollisionPair> collisions = new HashSet<>();

		// Skip if there are fewer than 2 colliders
		if (colliderNodes.size() < 2) {
			return collisions;
		}

		// STEP 1: Broadphase - find potential collision pairs
		Set<CollisionPair> potentialCollisions = broadphase.findPotentialCollisions(colliderNodes);

		// STEP 2: Narrowphase - detailed collision check for each potential pair
		for (CollisionPair pair : potentialCollisions) {
			// Get the entities and colliders
			ColliderNode nodeA = pair.getNodeA();
			ColliderNode nodeB = pair.getNodeB();

			// Skip if either node is no longer valid
			if (!isNodeValid(nodeA) || !isNodeValid(nodeB)) {
				continue;
			}

			// Check for layer filtering
			if (!canLayersCollide(nodeA.collider.getLayer(), nodeB.collider.getLayer())) {
				continue;
			}

			// Skip if both are triggers (they don't interact physically)
			if (nodeA.collider.isTrigger() && nodeB.collider.isTrigger()) {
				continue;
			}

			// Perform detailed collision check with contact information
			CollisionInfo info = performNarrowphaseCheck(nodeA, nodeB);

			if (info.colliding) {
				// Create final collision pair with contact information
				boolean isTrigger = nodeA.collider.isTrigger() || nodeB.collider.isTrigger();
				ContactPoint contact = new ContactPoint(
					info.contactPoint,
					info.contactNormal,
					info.penetrationDepth
				);

				// Add to final collision list
				collisions.add(new CollisionPair(nodeA, nodeB, contact, isTrigger));
			}
		}

		return collisions;
	}

	/**
	 * Performs detailed collision check between two colliders.
	 */
	private CollisionInfo performNarrowphaseCheck(ColliderNode nodeA, ColliderNode nodeB) {
		// Get collider shapes
		ICollisionShape shapeA = nodeA.collider.getCollisionShape();
		ICollisionShape shapeB = nodeB.collider.getCollisionShape();

		CollisionInfo info = new CollisionInfo();

		// Get world positions
		Vector2D posA = nodeA.transform.getPosition().add(nodeA.collider.getOffset());
		Vector2D posB = nodeB.transform.getPosition().add(nodeB.collider.getOffset());

		// Special case: circle vs circle
		if (shapeA instanceof CircleShape && shapeB instanceof CircleShape) {
			CircleShape circleA = (CircleShape) shapeA;
			CircleShape circleB = (CircleShape) shapeB;

			float radiusA = circleA.getRadius();
			float radiusB = circleB.getRadius();
			float radiusSum = radiusA + radiusB;

			// Calculate distance and direction
			Vector2D direction = posB.subtract(posA);
			float distanceSquared = direction.magnitudeSquared();

			// Check for collision
			if (distanceSquared < radiusSum * radiusSum) {
				float distance = (float) Math.sqrt(distanceSquared);
				Vector2D normal = direction.scale(1f / distance); // Normalized

				info.colliding = true;
				info.contactNormal = normal;
				info.penetrationDepth = radiusSum - distance;
				info.contactPoint = posA.add(normal.scale(radiusA));
			}

			return info;
		}

		// Circle vs Rectangle
		if (shapeA instanceof CircleShape && shapeB instanceof RectangleShape) {
			return checkCircleRectangle((CircleShape)shapeA, posA, (RectangleShape)shapeB, posB);
		}

		if (shapeA instanceof RectangleShape && shapeB instanceof CircleShape) {
			CollisionInfo reverseInfo = checkCircleRectangle(
				(CircleShape)shapeB, posB, (RectangleShape)shapeA, posA);

			// Flip normal direction for reversed check
			if (reverseInfo.colliding) {
				reverseInfo.contactNormal = reverseInfo.contactNormal.scale(-1);
			}

			return reverseInfo;
		}

		// Rectangle vs Rectangle
		if (shapeA instanceof RectangleShape && shapeB instanceof RectangleShape) {
			return checkRectangleRectangle(
				(RectangleShape)shapeA, posA, (RectangleShape)shapeB, posB);
		}

		// Default to simple shape intersection test
		boolean intersects = shapeA.intersects(shapeB);
		info.colliding = intersects;

		// For default case, we don't have detailed contact info
		if (intersects) {
			info.contactPoint = posA.add(posB).scale(0.5f); // Midpoint
			info.contactNormal = posB.subtract(posA).normalize();
			info.penetrationDepth = 0.1f; // Default small value
		}

		return info;
	}

	/**
	 * Checks for collision between a circle and rectangle.
	 */
	private CollisionInfo checkCircleRectangle(
		CircleShape circle, Vector2D circlePos,
		RectangleShape rect, Vector2D rectPos) {

		CollisionInfo info = new CollisionInfo();

		// Rectangle bounds
		float rectLeft = rectPos.x();
		float rectRight = rectPos.x() + rect.getWidth();
		float rectTop = rectPos.y();
		float rectBottom = rectPos.y() + rect.getHeight();

		// Find closest point on rectangle to circle center
		float closestX = Math.max(rectLeft, Math.min(circlePos.x(), rectRight));
		float closestY = Math.max(rectTop, Math.min(circlePos.y(), rectBottom));

		// Calculate distance to closest point
		Vector2D closestPoint = new Vector2D(closestX, closestY);
		Vector2D circleToClosest = closestPoint.subtract(circlePos);
		float distanceSquared = circleToClosest.magnitudeSquared();

		// Check collision
		float radius = circle.getRadius();
		if (distanceSquared < radius * radius) {
			float distance = (float) Math.sqrt(distanceSquared);

			// Check if we're dealing with zero distance
			Vector2D normal;
			if (distance < 0.0001f) {
				// Circle center is inside rectangle, use shortest exit direction
				float leftDist = circlePos.x() - rectLeft;
				float rightDist = rectRight - circlePos.x();
				float topDist = circlePos.y() - rectTop;
				float bottomDist = rectBottom - circlePos.y();

				// Find shortest exit direction
				float minDist = Math.min(Math.min(leftDist, rightDist), Math.min(topDist, bottomDist));

				if (minDist == leftDist) normal = new Vector2D(-1, 0);
				else if (minDist == rightDist) normal = new Vector2D(1, 0);
				else if (minDist == topDist) normal = new Vector2D(0, -1);
				else normal = new Vector2D(0, 1);

				info.penetrationDepth = minDist + radius;
			} else {
				// Normal points from circle to closest point
				normal = circleToClosest.scale(1f / distance);
				info.penetrationDepth = radius - distance;
			}

			info.colliding = true;
			info.contactNormal = normal;
			info.contactPoint = circlePos.add(normal.scale(radius));
		}

		return info;
	}

	/**
	 * Checks for collision between two rectangles.
	 */
	private CollisionInfo checkRectangleRectangle(
		RectangleShape rectA, Vector2D posA,
		RectangleShape rectB, Vector2D posB) {

		CollisionInfo info = new CollisionInfo();

		// Rectangle A bounds
		float leftA = posA.x();
		float rightA = posA.x() + rectA.getWidth();
		float topA = posA.y();
		float bottomA = posA.y() + rectA.getHeight();

		// Rectangle B bounds
		float leftB = posB.x();
		float rightB = posB.x() + rectB.getWidth();
		float topB = posB.y();
		float bottomB = posB.y() + rectB.getHeight();

		// Check for intersection
		if (leftA < rightB && rightA > leftB && topA < bottomB && bottomA > topB) {
			// Calculate overlap in each axis
			float overlapX = Math.min(rightA, rightB) - Math.max(leftA, leftB);
			float overlapY = Math.min(bottomA, bottomB) - Math.max(topA, topB);

			// Use minimum overlap as penetration depth
			if (overlapX < overlapY) {
				info.penetrationDepth = overlapX;
				info.contactNormal = posB.x() < posA.x() ?
					new Vector2D(-1, 0) : new Vector2D(1, 0);
			} else {
				info.penetrationDepth = overlapY;
				info.contactNormal = posB.y() < posA.y() ?
					new Vector2D(0, -1) : new Vector2D(0, 1);
			}

			// Calculate contact point at center of overlap region
			float contactX = Math.max(leftA, leftB) + overlapX / 2;
			float contactY = Math.max(topA, topB) + overlapY / 2;
			info.contactPoint = new Vector2D(contactX, contactY);

			info.colliding = true;
		}

		return info;
	}

	/**
	 * Checks if a node is valid for collision detection.
	 */
	public boolean isNodeValid(ColliderNode node) {
		return node != null &&
			node.getEntity() != null &&
			node.getEntity().getScene() != null &&
			node.transform != null &&
			node.collider != null;
	}

	/**
	 * Checks if two physics layers can collide with each other.
	 */
	private boolean canLayersCollide(PhysicsLayer layerA, PhysicsLayer layerB) {
		return layerMatrix.canLayersCollide(layerA, layerB);
	}

	/**
	 * Checks for collision between two colliders.
	 * Implementation for the ICollisionSPI interface.
	 */
	public boolean checkCollision(ICollider a, ICollider b) {
		if (a == null || b == null || a.getCollisionShape() == null || b.getCollisionShape() == null) {
			return false;
		}

		return a.getCollisionShape().intersects(b.getCollisionShape());
	}

	/**
	 * Checks for collision with a tile.
	 * Implementation for the ICollisionSPI interface.
	 */
	public boolean checkTileCollision(ICollider collider, int tileX, int tileY, int tileSize) {
		if (collider == null || collider.getCollisionShape() == null) {
			return false;
		}

		// Create rectangle shape for the tile
		RectangleShape tileShape = new RectangleShape(
			new Vector2D(tileX * tileSize, tileY * tileSize),
			tileSize,
			tileSize
		);

		return collider.getCollisionShape().intersects(tileShape);
	}

	/**
	 * Checks if a position is valid for movement.
	 * Implementation for the ICollisionSPI interface.
	 */
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

		// Get entity colliders for collision checking
		Set<ColliderNode> entityColliders = NodeManager.active().getNodes(ColliderNode.class);

		// First, check against tilemaps
		for (TilemapColliderNode tilemap : tilemapNodes) {
			if (!isTilemapPositionValid(collider, proposedPosition, tilemap)) {
				return false;
			}
		}

		// Then check against other entity colliders
		Entity colliderEntity = collider instanceof ColliderComponent ?
			((ColliderComponent) collider).getEntity() : null;

		for (ColliderNode node : entityColliders) {
			if (node.getEntity().hasComponent(PhysicsComponent.class))
				continue;

			// Skip invalid nodes and self-collision
			if (!isNodeValid(node) || node.getEntity() == colliderEntity) {
				continue;
			}

			// Skip triggers
			if (node.collider.isTrigger()) {
				continue;
			}

			// Create a copy of the collider at the proposed position
			ICollisionShape proposedShape = createShapeAtPosition(
				collider.getCollisionShape(),
				proposedPosition,
				collider instanceof ColliderComponent ?
					((ColliderComponent) collider).getOffset() : new Vector2D(0, 0)
			);

			// Get entity collider's world shape
			Vector2D nodePosition = node.transform.getPosition();
			Vector2D nodeOffset = node.collider.getOffset();
			ICollisionShape nodeWorldShape = createShapeAtPosition(
				node.collider.getCollisionShape(),
				nodePosition,
				nodeOffset
			);

			// Check for intersection
			if (proposedShape.intersects(nodeWorldShape)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Creates a collision shape at a specific world position.
	 */
	private ICollisionShape createShapeAtPosition(ICollisionShape baseShape, Vector2D position, Vector2D offset) {
		Vector2D worldPos = position.add(offset);

		if (baseShape instanceof CircleShape) {
			CircleShape circle = (CircleShape) baseShape;
			return new CircleShape(worldPos, circle.getRadius());
		}
		else if (baseShape instanceof RectangleShape) {
			RectangleShape rect = (RectangleShape) baseShape;
			return new RectangleShape(worldPos, rect.getWidth(), rect.getHeight());
		}

		// Default fallback - shouldn't happen
		return baseShape;
	}

	/**
	 * Checks if a position is valid for movement (specific to tilemap).
	 */
	private boolean isTilemapPositionValid(ICollider collider, Vector2D position, TilemapColliderNode tilemap) {
		if (tilemap == null || tilemap.transform == null ||
			tilemap.tilemap == null || tilemap.tilemapCollider == null) {
			return true;
		}

		// Get tilemap properties
		int tileSize = tilemap.tilemap.getTileSize();
		Vector2D tilemapPos = tilemap.transform.getPosition();
		int[][] collisionFlags = tilemap.tilemapCollider.getCollisionFlags();

		// Skip if collision flags are missing
		if (collisionFlags == null) {
			return true;
		}

		// For a circle collider
		if (collider.getCollisionShape() instanceof CircleShape) {
			CircleShape circleShape = (CircleShape) collider.getCollisionShape();
			float radius = circleShape.getRadius();

			// Calculate the adjusted position of the collider
			Vector2D colliderOffset = new Vector2D(0, 0);
			if (collider instanceof ColliderComponent) {
				colliderOffset = ((ColliderComponent) collider).getOffset();
			}
			Vector2D worldPos = position.add(colliderOffset);

			// Calculate the grid bounds we need to check
			// Convert world position to tilemap-relative coordinates
			Vector2D relativePos = worldPos.subtract(tilemapPos);

			// Determine the tile indices range to check
			int centerTileX = (int) Math.floor(relativePos.x() / tileSize);
			int centerTileY = (int) Math.floor(relativePos.y() / tileSize);

			// How many tiles to check in each direction from center tile
			int tileCheckRange = (int) Math.ceil(radius / tileSize) + 1;

			// Check surrounding tiles
			for (int y = centerTileY - tileCheckRange; y <= centerTileY + tileCheckRange; y++) {
				for (int x = centerTileX - tileCheckRange; x <= centerTileX + tileCheckRange; x++) {
					// Skip tiles outside the tilemap
					if (x < 0 || y < 0 || x >= collisionFlags.length ||
						(collisionFlags.length > 0 && y >= collisionFlags[0].length)) {
						continue;
					}

					// Skip non-solid tiles
					if (collisionFlags[x][y] == 0) {
						continue;
					}

					// This is a solid tile - check for collision
					Vector2D tilePos = tilemapPos.add(new Vector2D(x * tileSize, y * tileSize));
					RectangleShape tileShape = new RectangleShape(tilePos, tileSize, tileSize);

					// Create a new circle at the proposed position
					CircleShape proposedCircle = new CircleShape(worldPos, radius);

					// If they intersect, the position is invalid
					if (checkCircleRectIntersection(proposedCircle, tileShape)) {
						return false;
					}
				}
			}
		}
		// For a rectangle collider
		else if (collider.getCollisionShape() instanceof RectangleShape) {
			RectangleShape rectShape = (RectangleShape) collider.getCollisionShape();

			// Calculate the adjusted position of the collider
			Vector2D colliderOffset = new Vector2D(0, 0);
			if (collider instanceof ColliderComponent) {
				colliderOffset = ((ColliderComponent) collider).getOffset();
			}
			Vector2D worldPos = position.add(colliderOffset);

			// Get rectangle dimensions
			float width = rectShape.getWidth();
			float height = rectShape.getHeight();

			// Create rectangle at proposed position
			RectangleShape proposedRect = new RectangleShape(worldPos, width, height);

			// Convert to tilemap coordinates
			Vector2D relativePos = worldPos.subtract(tilemapPos);

			// Calculate tile indices range
			int minTileX = (int) Math.floor((relativePos.x() - width/2) / tileSize);
			int maxTileX = (int) Math.ceil((relativePos.x() + width/2) / tileSize);
			int minTileY = (int) Math.floor((relativePos.y() - height/2) / tileSize);
			int maxTileY = (int) Math.ceil((relativePos.y() + height/2) / tileSize);

			// Clamp to tilemap bounds
			minTileX = Math.max(0, minTileX);
			minTileY = Math.max(0, minTileY);
			maxTileX = Math.min(collisionFlags.length - 1, maxTileX);
			maxTileY = Math.min(collisionFlags[0].length - 1, maxTileY);

			// Check each tile in the range
			for (int y = minTileY; y <= maxTileY; y++) {
				for (int x = minTileX; x <= maxTileX; x++) {
					// Skip non-solid tiles
					if (collisionFlags[x][y] == 0) {
						continue;
					}

					// This is a solid tile - check for collision
					Vector2D tilePos = tilemapPos.add(new Vector2D(x * tileSize, y * tileSize));
					RectangleShape tileShape = new RectangleShape(tilePos, tileSize, tileSize);

					// If they intersect, the position is invalid
					if (proposedRect.intersects(tileShape)) {
						return false;
					}
				}
			}
		}

		// No collisions found, position is valid
		return true;
	}

	/**
	 * Helper method to check if a circle intersects with a rectangle.
	 */
	private boolean checkCircleRectIntersection(CircleShape circle, RectangleShape rect) {
		Vector2D circleCenter = circle.getCenter();
		float radius = circle.getRadius();

		// Get rectangle bounds
		Vector2D rectPos = rect.getPosition();
		float rectWidth = rect.getWidth();
		float rectHeight = rect.getHeight();

		// Find closest point on rectangle to circle center
		float closestX = Math.max(rectPos.x(), Math.min(circleCenter.x(), rectPos.x() + rectWidth));
		float closestY = Math.max(rectPos.y(), Math.min(circleCenter.y(), rectPos.y() + rectHeight));

		// Calculate distance squared from closest point to circle center
		Vector2D closestPoint = new Vector2D(closestX, closestY);
		float distanceSquared = closestPoint.subtract(circleCenter).magnitudeSquared();

		// If distance is less than radius, they intersect
		return distanceSquared < radius * radius;
	}

	/**
	 * Internal class to store detailed collision information.
	 */
	private static class CollisionInfo {
		boolean colliding = false;
		Vector2D contactPoint = null;
		Vector2D contactNormal = null;
		float penetrationDepth = 0;
	}
}