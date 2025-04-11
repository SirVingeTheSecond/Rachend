package dk.sdu.sem.collisionsystem.detection;

import dk.sdu.sem.collision.*;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.shapes.BoxShape;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.collisionsystem.*;
import dk.sdu.sem.collisionsystem.utils.NodeValidator;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonsystem.TransformComponent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles collision detection between entities and tilemaps.
 */
public class CollisionDetector {
	private final LayerCollisionMatrix layerMatrix;
	private static final float COLLISION_THRESHOLD = 0.03f; // Skin width for consistent detection

	public CollisionDetector(LayerCollisionMatrix layerMatrix) {
		this.layerMatrix = layerMatrix;
	}

	/**
	 * Detects collisions between all colliders.
	 *
	 * @param colliderNodes Set of collider nodes to check
	 * @return List of collision pairs detected
	 */
	public List<CollisionPair> detectCollisions(Set<ColliderNode> colliderNodes) {
		List<CollisionPair> collisions = new ArrayList<>();

		// Quick exit if there are too few nodes to have collisions
		if (colliderNodes.size() < 2) {
			return collisions;
		}

		// Filter out invalid nodes before processing
		List<ColliderNode> validNodes = colliderNodes.stream()
			.filter(node -> NodeValidator.isColliderNodeValid(node) && node.collider.isEnabled())
			.collect(Collectors.toList());

		// Use spatial partitioning for better performance with many objects
		Map<Integer, List<ColliderNode>> spatialBuckets = createSpatialBuckets(validNodes);

		// Check collisions
		for (int i = 0; i < validNodes.size(); i++) {
			ColliderNode nodeA = validNodes.get(i);

			// Get potential collision candidates from spatial buckets
			List<ColliderNode> potentialColliders = getPotentialColliders(nodeA, spatialBuckets);

			for (ColliderNode nodeB : potentialColliders) {
				// Skip self-collision and already checked pairs
				if (nodeB == nodeA) {
					continue;
				}

				// Check layer filtering
				if (!canLayersCollide(nodeA.collider.getLayer(), nodeB.collider.getLayer())) {
					continue;
				}

				// Perform detailed collision check
				CollisionInfo collisionInfo = checkCollision(nodeA, nodeB);
				if (collisionInfo.isColliding()) {
					addCollisionPair(collisions, nodeA, nodeB, collisionInfo);
				}
			}
		}

		return collisions;
	}

	/**
	 * Creates a collision pair and adds it to the list
	 */
	private void addCollisionPair(List<CollisionPair> collisions, ColliderNode nodeA, ColliderNode nodeB, CollisionInfo info) {
		boolean isTrigger = nodeA.collider.isTrigger() || nodeB.collider.isTrigger();
		ContactPoint contact = new ContactPoint(
			info.getContactPoint(),
			info.getContactNormal(),
			info.getPenetrationDepth()
		);

		CollisionPair pair = new CollisionPair(
			nodeA.getEntity(),
			nodeB.getEntity(),
			nodeA.collider,
			nodeB.collider,
			contact,
			isTrigger
		);

		collisions.add(pair);
	}

	/**
	 * Detects collisions between colliders and tilemaps.
	 *
	 * @param colliderNodes Set of collider nodes
	 * @param tilemapNodes Set of tilemap nodes
	 * @return List of collision pairs detected
	 */
	public List<CollisionPair> detectTilemapCollisions(
		Set<ColliderNode> colliderNodes,
		Set<TilemapColliderNode> tilemapNodes) {

		List<CollisionPair> collisions = new ArrayList<>();

		// Quick exit if either set is empty
		if (colliderNodes.isEmpty() || tilemapNodes.isEmpty()) {
			return collisions;
		}

		// Filter valid collider nodes
		List<ColliderNode> validColliders = colliderNodes.stream()
			.filter(NodeValidator::isColliderNodeValid)
			.collect(Collectors.toList());

		// Filter valid tilemap nodes
		List<TilemapColliderNode> validTilemaps = tilemapNodes.stream()
			.filter(NodeValidator::isTilemapNodeValid)
			.collect(Collectors.toList());

		for (ColliderNode colliderNode : validColliders) {
			Entity colliderEntity = colliderNode.getEntity();
			ColliderComponent collider = colliderNode.collider;
			Vector2D colliderPos = colliderNode.transform.getPosition().add(collider.getOffset());
			PhysicsLayer colliderLayer = collider.getLayer();

			for (TilemapColliderNode tilemapNode : validTilemaps) {
				// Check layer filtering
				PhysicsLayer tilemapLayer = tilemapNode.tilemapCollider.getLayer();
				if (!canLayersCollide(colliderLayer, tilemapLayer)) {
					continue;
				}

				// Extract required parameters from the nodes
				Vector2D tilemapPos = tilemapNode.transform.getPosition();
				int tileSize = tilemapNode.tilemap.getTileSize();
				int[][] collisionFlags = tilemapNode.tilemapCollider.getCollisionFlags();

				// Perform tilemap collision check
				TilemapCollisionResult result = checkTilemapCollision(
					colliderNode,
					tilemapNode,
					colliderPos,
					tilemapPos,
					tileSize,
					collisionFlags
				);

				if (result.isColliding()) {
					addTilemapCollisionPair(collisions, colliderNode, tilemapNode, result);
				}
			}
		}

		return collisions;
	}

	/**
	 * Creates a tilemap collision pair and adds it to the list
	 */
	private void addTilemapCollisionPair(List<CollisionPair> collisions,
										 ColliderNode colliderNode,
										 TilemapColliderNode tilemapNode,
										 TilemapCollisionResult result) {
		ContactPoint contact = new ContactPoint(
			result.getContactPoint(),
			result.getContactNormal(),
			result.getPenetrationDepth()
		);

		boolean isTrigger = colliderNode.collider.isTrigger();

		CollisionPair pair = new TilemapCollisionPair(
			colliderNode,
			tilemapNode,
			result.getTileX(),
			result.getTileY(),
			contact,
			isTrigger
		);

		collisions.add(pair);
	}

	/**
	 * Creates spatial buckets for broad phase collision detection.
	 */
	private Map<Integer, List<ColliderNode>> createSpatialBuckets(List<ColliderNode> nodes) {
		Map<Integer, List<ColliderNode>> buckets = new HashMap<>();
		float bucketSize = 100.0f; // Adjust based on game scale

		for (ColliderNode node : nodes) {
			Vector2D position = node.transform.getPosition();
			int bucketX = (int) (position.x() / bucketSize);
			int bucketY = (int) (position.y() / bucketSize);

			// Get bucket hash
			int bucketHash = bucketX * 73856093 ^ bucketY * 19349663; // Spatial hash function

			// Add to bucket
			buckets.computeIfAbsent(bucketHash, k -> new ArrayList<>()).add(node);

			// Also add to neighboring buckets for objects near bucket boundaries
			addToNeighboringBuckets(buckets, node, bucketX, bucketY, bucketSize);
		}

		return buckets;
	}

	/**
	 * Adds a node to its neighboring buckets
	 */
	private void addToNeighboringBuckets(Map<Integer, List<ColliderNode>> buckets,
										 ColliderNode node,
										 int bucketX,
										 int bucketY,
										 float bucketSize) {
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				if (dx == 0 && dy == 0) continue; // Skip current bucket

				int neighborX = bucketX + dx;
				int neighborY = bucketY + dy;
				int neighborHash = neighborX * 73856093 ^ neighborY * 19349663;

				buckets.computeIfAbsent(neighborHash, k -> new ArrayList<>()).add(node);
			}
		}
	}

	/**
	 * Gets potential colliders from spatial buckets.
	 */
	private List<ColliderNode> getPotentialColliders(ColliderNode node, Map<Integer, List<ColliderNode>> buckets) {
		Vector2D position = node.transform.getPosition();
		float bucketSize = 100.0f;

		int bucketX = (int) (position.x() / bucketSize);
		int bucketY = (int) (position.y() / bucketSize);
		int bucketHash = bucketX * 73856093 ^ bucketY * 19349663;

		return buckets.getOrDefault(bucketHash, new ArrayList<>());
	}

	/**
	 * Checks collision between two colliders.
	 */
	private CollisionInfo checkCollision(ColliderNode nodeA, ColliderNode nodeB) {
		// Get world positions
		Vector2D posA = nodeA.transform.getPosition().add(nodeA.collider.getOffset());
		Vector2D posB = nodeB.transform.getPosition().add(nodeB.collider.getOffset());

		ICollisionShape shapeA = nodeA.collider.getShape();
		ICollisionShape shapeB = nodeB.collider.getShape();

		CollisionInfo info = new CollisionInfo();

		// Check different shape combinations
		if (shapeA instanceof CircleShape && shapeB instanceof CircleShape) {
			checkCircleCircle((CircleShape)shapeA, posA, (CircleShape)shapeB, posB, info);
		}
		else if (shapeA instanceof CircleShape && shapeB instanceof BoxShape) {
			checkCircleBox((CircleShape)shapeA, posA, (BoxShape)shapeB, posB, info);
		}
		else if (shapeA instanceof BoxShape && shapeB instanceof CircleShape) {
			checkCircleBox((CircleShape)shapeB, posB, (BoxShape)shapeA, posA, info);
			// Flip normal direction
			if (info.isColliding()) {
				info.setContactNormal(info.getContactNormal().scale(-1));
			}
		}
		else if (shapeA instanceof BoxShape && shapeB instanceof BoxShape) {
			checkBoxBox((BoxShape)shapeA, posA, (BoxShape)shapeB, posB, info);
		}

		return info;
	}

	/**
	 * Checks collision between a collider and a tilemap.
	 */
	private TilemapCollisionResult checkTilemapCollision(
		ColliderNode colliderNode,
		TilemapColliderNode tilemapNode,
		Vector2D colliderPos,
		Vector2D tilemapPos,
		int tileSize,
		int[][] collisionMap) {

		TilemapCollisionResult result = new TilemapCollisionResult();
		ICollisionShape shape = colliderNode.collider.getShape();

		if (shape instanceof CircleShape) {
			checkCircleTilemap((CircleShape)shape, colliderPos, tilemapPos, tileSize, collisionMap, result);
		}
		else if (shape instanceof BoxShape) {
			checkBoxTilemap((BoxShape)shape, colliderPos, tilemapPos, tileSize, collisionMap, result);
		}

		return result;
	}

	/**
	 * Checks collision between a circle and tilemap
	 */
	private void checkCircleTilemap(CircleShape circle,
									Vector2D circlePos,
									Vector2D tilemapPos,
									int tileSize,
									int[][] collisionMap,
									TilemapCollisionResult result) {
		float radius = circle.getRadius();

		// Convert to tilemap coordinates
		Vector2D relativePosA = circlePos.subtract(tilemapPos);

		// Determine tile range to check
		int minTileX = (int) Math.floor((relativePosA.x() - radius) / tileSize);
		int maxTileX = (int) Math.ceil((relativePosA.x() + radius) / tileSize);
		int minTileY = (int) Math.floor((relativePosA.y() - radius) / tileSize);
		int maxTileY = (int) Math.ceil((relativePosA.y() + radius) / tileSize);

		// Clamp to map bounds
		minTileX = Math.max(0, minTileX);
		minTileY = Math.max(0, minTileY);
		maxTileX = Math.min(collisionMap.length - 1, maxTileX);
		maxTileY = Math.min(collisionMap[0].length - 1, maxTileY);

		// Check each tile
		for (int y = minTileY; y <= maxTileY; y++) {
			for (int x = minTileX; x <= maxTileX; x++) {
				// Skip non-solid tiles
				if (collisionMap[x][y] == 0) {
					continue;
				}

				// Create tile box
				Vector2D tilePos = tilemapPos.add(new Vector2D(x * tileSize, y * tileSize));
				BoxShape tileBox = new BoxShape(tileSize, tileSize);

				// Check circle-box collision
				CollisionInfo info = new CollisionInfo();
				checkCircleBox(circle, circlePos, tileBox, tilePos, info);

				if (info.isColliding()) {
					result.setColliding(true);
					result.setContactPoint(info.getContactPoint());
					result.setContactNormal(info.getContactNormal());
					result.setPenetrationDepth(info.getPenetrationDepth());
					result.setTileX(x);
					result.setTileY(y);
					return; // Return on first collision
				}
			}
		}
	}

	/**
	 * Checks collision between a box and tilemap
	 */
	private void checkBoxTilemap(BoxShape box,
								 Vector2D boxPos,
								 Vector2D tilemapPos,
								 int tileSize,
								 int[][] collisionMap,
								 TilemapCollisionResult result) {
		float width = box.getWidth();
		float height = box.getHeight();

		// Convert to tilemap coordinates
		Vector2D relativePosA = boxPos.subtract(tilemapPos);

		// Determine tile range to check
		int minTileX = (int) Math.floor(relativePosA.x() / tileSize);
		int maxTileX = (int) Math.ceil((relativePosA.x() + width) / tileSize);
		int minTileY = (int) Math.floor(relativePosA.y() / tileSize);
		int maxTileY = (int) Math.ceil((relativePosA.y() + height) / tileSize);

		// Clamp to map bounds
		minTileX = Math.max(0, minTileX);
		minTileY = Math.max(0, minTileY);
		maxTileX = Math.min(collisionMap.length - 1, maxTileX);
		maxTileY = Math.min(collisionMap[0].length - 1, maxTileY);

		// Check each tile
		for (int y = minTileY; y <= maxTileY; y++) {
			for (int x = minTileX; x <= maxTileX; x++) {
				// Skip non-solid tiles
				if (collisionMap[x][y] == 0) {
					continue;
				}

				// Create tile box
				Vector2D tilePos = tilemapPos.add(new Vector2D(x * tileSize, y * tileSize));
				BoxShape tileBox = new BoxShape(tileSize, tileSize);

				// Check box-box collision
				CollisionInfo info = new CollisionInfo();
				checkBoxBox(box, boxPos, tileBox, tilePos, info);

				if (info.isColliding()) {
					result.setColliding(true);
					result.setContactPoint(info.getContactPoint());
					result.setContactNormal(info.getContactNormal());
					result.setPenetrationDepth(info.getPenetrationDepth());
					result.setTileX(x);
					result.setTileY(y);
					return; // Return on first collision
				}
			}
		}
	}

	/**
	 * Checks collision between two circles.
	 */
	private void checkCircleCircle(CircleShape circleA, Vector2D posA, CircleShape circleB, Vector2D posB,
								   CollisionInfo info) {
		float radiusA = circleA.getRadius();
		float radiusB = circleB.getRadius();
		float radiusSum = radiusA + radiusB;

		// Calculate vector between centers
		Vector2D direction = posB.subtract(posA);
		float distanceSquared = direction.magnitudeSquared();

		// Check for collision
		if (distanceSquared < radiusSum * radiusSum) {
			float distance = (float) Math.sqrt(distanceSquared);

			// Normalize direction
			Vector2D normal = distance > 0.0001f ?
				direction.scale(1f / distance) : new Vector2D(1, 0);

			info.setColliding(true);
			info.setContactNormal(normal);
			info.setPenetrationDepth(radiusSum - distance);
			info.setContactPoint(posA.add(normal.scale(radiusA)));
		}
	}

	/**
	 * Checks collision between a circle and a box.
	 */
	private void checkCircleBox(CircleShape circle, Vector2D circlePos, BoxShape box, Vector2D boxPos,
								CollisionInfo info) {
		float radius = circle.getRadius();
		float boxWidth = box.getWidth();
		float boxHeight = box.getHeight();

		// Calculate box bounds
		float boxLeft = boxPos.x();
		float boxRight = boxPos.x() + boxWidth;
		float boxTop = boxPos.y();
		float boxBottom = boxPos.y() + boxHeight;

		// Find closest point on box to circle center
		float closestX = Math.max(boxLeft, Math.min(circlePos.x(), boxRight));
		float closestY = Math.max(boxTop, Math.min(circlePos.y(), boxBottom));

		// Calculate vector from closest point to circle center
		Vector2D closestPoint = new Vector2D(closestX, closestY);
		Vector2D toCircle = circlePos.subtract(closestPoint);
		float distanceSquared = toCircle.magnitudeSquared();

		// Check for collision
		if (distanceSquared < radius * radius) {
			float distance = (float) Math.sqrt(distanceSquared);

			// Calculate normal
			Vector2D normal;
			if (distance < 0.0001f) {
				// Circle center is inside box, find closest edge
				float leftDist = circlePos.x() - boxLeft;
				float rightDist = boxRight - circlePos.x();
				float topDist = circlePos.y() - boxTop;
				float bottomDist = boxBottom - circlePos.y();

				// Find smallest distance
				float minDist = Math.min(Math.min(leftDist, rightDist), Math.min(topDist, bottomDist));

				if (minDist == leftDist) normal = new Vector2D(-1, 0);
				else if (minDist == rightDist) normal = new Vector2D(1, 0);
				else if (minDist == topDist) normal = new Vector2D(0, -1);
				else normal = new Vector2D(0, 1);

				info.setPenetrationDepth(radius + minDist);
			} else {
				// Normal points from closest point to circle center
				normal = distance > 0.0001f ?
					toCircle.scale(1f / distance) : new Vector2D(1, 0);
				info.setPenetrationDepth(radius - distance);
			}

			info.setColliding(true);
			info.setContactNormal(normal);
			info.setContactPoint(circlePos.subtract(normal.scale(radius)));
		}
	}

	/**
	 * Checks collision between two boxes.
	 */
	private void checkBoxBox(BoxShape boxA, Vector2D posA, BoxShape boxB, Vector2D posB,
							 CollisionInfo info) {
		float widthA = boxA.getWidth();
		float heightA = boxA.getHeight();
		float widthB = boxB.getWidth();
		float heightB = boxB.getHeight();

		// Calculate bounds
		float leftA = posA.x();
		float rightA = posA.x() + widthA;
		float topA = posA.y();
		float bottomA = posA.y() + heightA;

		float leftB = posB.x();
		float rightB = posB.x() + widthB;
		float topB = posB.y();
		float bottomB = posB.y() + heightB;

		// Check for intersection (AABB test)
		if (leftA < rightB && rightA > leftB && topA < bottomB && bottomA > topB) {
			// Calculate overlap on each axis
			float overlapX = Math.min(rightA, rightB) - Math.max(leftA, leftB);
			float overlapY = Math.min(bottomA, bottomB) - Math.max(topA, topB);

			// Use smaller overlap to determine collision normal
			Vector2D normal;
			if (overlapX < overlapY) {
				// X axis has smaller overlap - determine direction
				normal = (posA.x() < posB.x()) ? new Vector2D(1, 0) : new Vector2D(-1, 0);
				info.setPenetrationDepth(overlapX);
			} else {
				// Y axis has smaller overlap - determine direction
				normal = (posA.y() < posB.y()) ? new Vector2D(0, 1) : new Vector2D(0, -1);
				info.setPenetrationDepth(overlapY);
			}

			// Calculate contact point at center of overlap region
			float contactX = Math.max(leftA, leftB) + (overlapX / 2);
			float contactY = Math.max(topA, topB) + (overlapY / 2);

			info.setColliding(true);
			info.setContactNormal(normal);
			info.setContactPoint(new Vector2D(contactX, contactY));
		}
	}

	/**
	 * Checks if two layers can collide.
	 */
	private boolean canLayersCollide(PhysicsLayer layerA, PhysicsLayer layerB) {
		return layerMatrix.canLayersCollide(layerA, layerB);
	}

	/**
	 * Casts a ray against all colliders in the scene.
	 */
	public RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance) {
		// Implementation would go here
		return RaycastHit.noHit();
	}

	/**
	 * Casts a ray against colliders in specific layers.
	 */
	public RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance, PhysicsLayer layer) {
		// Implementation would go here
		return RaycastHit.noHit();
	}

	/**
	 * Checks collision between two colliders.
	 */
	public boolean checkCollision(ColliderComponent colliderA, ColliderComponent colliderB) {
		if (!colliderA.isEnabled() || !colliderB.isEnabled()) {
			return false;
		}

		if (!canLayersCollide(colliderA.getLayer(), colliderB.getLayer())) {
			return false;
		}

		Vector2D posA = colliderA.getWorldPosition();
		Vector2D posB = colliderB.getWorldPosition();

		ICollisionShape shapeA = colliderA.getShape();
		ICollisionShape shapeB = colliderB.getShape();

		// Simple intersection test
		if (shapeA instanceof CircleShape && shapeB instanceof CircleShape) {
			// Circle vs circle
			CircleShape circleA = (CircleShape)shapeA;
			CircleShape circleB = (CircleShape)shapeB;
			float radiusSum = circleA.getRadius() + circleB.getRadius();
			return posA.subtract(posB).magnitudeSquared() < radiusSum * radiusSum;
		}
		else if (shapeA instanceof CircleShape && shapeB instanceof BoxShape) {
			// Circle vs box - use detailed check
			CollisionInfo info = new CollisionInfo();
			checkCircleBox((CircleShape)shapeA, posA, (BoxShape)shapeB, posB, info);
			return info.isColliding();
		}
		else if (shapeA instanceof BoxShape && shapeB instanceof CircleShape) {
			// Box vs circle - use detailed check
			CollisionInfo info = new CollisionInfo();
			checkCircleBox((CircleShape)shapeB, posB, (BoxShape)shapeA, posA, info);
			return info.isColliding();
		}
		else if (shapeA instanceof BoxShape && shapeB instanceof BoxShape) {
			// Box vs box - use detailed check
			CollisionInfo info = new CollisionInfo();
			checkBoxBox((BoxShape)shapeA, posA, (BoxShape)shapeB, posB, info);
			return info.isColliding();
		}

		return false;
	}

	/**
	 * Stores information about a collision.
	 */
	private static class CollisionInfo {
		private boolean colliding = false;
		private Vector2D contactPoint = null;
		private Vector2D contactNormal = null;
		private float penetrationDepth = 0;

		public boolean isColliding() {
			return colliding;
		}

		public void setColliding(boolean colliding) {
			this.colliding = colliding;
		}

		public Vector2D getContactPoint() {
			return contactPoint;
		}

		public void setContactPoint(Vector2D contactPoint) {
			this.contactPoint = contactPoint;
		}

		public Vector2D getContactNormal() {
			return contactNormal;
		}

		public void setContactNormal(Vector2D contactNormal) {
			this.contactNormal = contactNormal;
		}

		public float getPenetrationDepth() {
			return penetrationDepth;
		}

		public void setPenetrationDepth(float penetrationDepth) {
			this.penetrationDepth = penetrationDepth;
		}
	}

	/**
	 * Stores information about a tilemap collision.
	 */
	private static class TilemapCollisionResult extends CollisionInfo {
		private int tileX = -1;
		private int tileY = -1;

		public int getTileX() {
			return tileX;
		}

		public void setTileX(int tileX) {
			this.tileX = tileX;
		}

		public int getTileY() {
			return tileY;
		}

		public void setTileY(int tileY) {
			this.tileY = tileY;
		}
	}
}