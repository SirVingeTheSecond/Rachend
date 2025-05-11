package dk.sdu.sem.collisionsystem.broadphase;

import dk.sdu.sem.collision.data.AABB;
import dk.sdu.sem.collision.data.CollisionPair;
import dk.sdu.sem.collision.shapes.*;
import dk.sdu.sem.collisionsystem.nodes.ColliderNode;
import dk.sdu.sem.collisionsystem.utils.NodeValidator;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Broadphase strategy using a QuadTree for spatial partitioning.
 * Finds potential collision pairs without detailed collision checks.
 */
public class QuadTreeBroadphase implements BroadphaseStrategy {
	private static final int MAX_DEPTH = 5;
	private static final int MAX_OBJECTS_PER_NODE = 10;
	private static final float DEFAULT_WORLD_SIZE = 2000.0f;
	private static final float EPSILON = 0.0001f;
	private static final boolean USE_DYNAMIC_BOUNDS = true;

	private QuadTree quadRoot;

	@Override
	public Set<CollisionPair> findPotentialCollisions(Set<ColliderNode> colliderNodes) {
		Set<CollisionPair> potentialCollisions = new HashSet<>();

		// Skip if too few nodes to have collisions
		if (colliderNodes.size() < 2) {
			return potentialCollisions;
		}

		// Filter valid nodes
		List<ColliderNode> validNodes = colliderNodes.stream()
			.filter(NodeValidator::isColliderNodeValid)
			.collect(Collectors.toList());

		if (validNodes.size() < 2) {
			return potentialCollisions;
		}

		// Create quadtree with appropriate bounds
		AABB worldBounds = USE_DYNAMIC_BOUNDS ?
			calculateDynamicBounds(validNodes) :
			getDefaultWorldBounds();

		// Store the quadtree reference for raycasting
		quadRoot = new QuadTree(worldBounds, 0);

		// Insert all nodes into quadtree
		for (ColliderNode node : validNodes) {
			quadRoot.insert(node);
		}

		// For each node, find potential collision partners
		for (ColliderNode nodeA : validNodes) {
			//No need to check static on static collisions
			if (!nodeA.getEntity().hasComponent(PhysicsComponent.class))
				continue;

			// Skip if node is invalid
			if (!NodeValidator.isColliderNodeValid(nodeA)) {
				continue;
			}

			// Get potential collision partners
			Set<ColliderNode> potentialPartners = quadRoot.getPotentialCollisions(nodeA);

			// Create pairs, ensuring no duplicates
			for (ColliderNode nodeB : potentialPartners) {
				// Skip self-collision
				if (nodeB == nodeA) {
					continue;
				}

				// Create collision pair with appropriate trigger flag
				boolean isTrigger = nodeA.collider.isTrigger() || nodeB.collider.isTrigger();

				potentialCollisions.add(new CollisionPair(
					nodeA.getEntity(),
					nodeB.getEntity(),
					nodeA.collider,
					nodeB.collider,
					null, // Contact point determined in narrow phase
					isTrigger
				));
			}
		}

		return potentialCollisions;
	}

	/**
	 * Calculates appropriate world bounds based on entity positions
	 */
	private AABB calculateDynamicBounds(List<ColliderNode> nodes) {
		if (nodes.isEmpty()) {
			return getDefaultWorldBounds();
		}

		// Start with the first node
		ColliderNode first = nodes.get(0);
		Vector2D pos = first.transform.getPosition();
		float radius = getColliderRadius(first);

		float minX = pos.x() - radius;
		float minY = pos.y() - radius;
		float maxX = pos.x() + radius;
		float maxY = pos.y() + radius;

		// Expand to include all nodes
		for (int i = 1; i < nodes.size(); i++) {
			ColliderNode node = nodes.get(i);
			Vector2D position = node.transform.getPosition();
			radius = getColliderRadius(node);

			minX = Math.min(minX, position.x() - radius);
			minY = Math.min(minY, position.y() - radius);
			maxX = Math.max(maxX, position.x() + radius);
			maxY = Math.max(maxY, position.y() + radius);
		}

		// Add padding
		float padding = 200.0f;
		return new AABB(
			minX - padding,
			minY - padding,
			maxX + padding,
			maxY + padding
		);
	}

	/**
	 * Default world bounds when no entities exist
	 */
	private AABB getDefaultWorldBounds() {
		return new AABB(
			-DEFAULT_WORLD_SIZE,
			-DEFAULT_WORLD_SIZE,
			DEFAULT_WORLD_SIZE,
			DEFAULT_WORLD_SIZE
		);
	}

	/**
	 * Gets the radius of a collider for bounds calculation
	 */
	private float getColliderRadius(ColliderNode node) {
		ICollisionShape shape = node.collider.getShape();
		if (shape instanceof CircleShape) {
			return ((CircleShape) shape).getRadius();
		} else if (shape instanceof BoxShape) {
			BoxShape box = (BoxShape) shape;
			return (float) Math.sqrt(box.getWidth() * box.getWidth() +
				box.getHeight() * box.getHeight()) / 2.0f;
		}
		return 10.0f; // Default size
	}

	/**
	 * Gets an AABB for a collider node. Moved from the QuadTree class to make it accessible
	 * from the outer class methods.
	 */
	private AABB getNodeAABB(ColliderNode node) {
		ICollisionShape shape = node.collider.getShape();
		Vector2D position = node.transform.getPosition().add(node.collider.getOffset());

		if (shape instanceof CircleShape) {
			float radius = ((CircleShape) shape).getRadius();
			return new AABB(
				position.x() - radius,
				position.y() - radius,
				position.x() + radius,
				position.y() + radius
			);
		} else if (shape instanceof BoxShape) {
			BoxShape box = (BoxShape) shape;
			return new AABB(
				position.x(),
				position.y(),
				position.x() + box.getWidth(),
				position.y() + box.getHeight()
			);
		} else if (shape instanceof GridShape) {
			GridShape grid = (GridShape) shape;
			Bounds bounds = grid.getBounds();
			return new AABB(
				position.x() + bounds.getMinX(),
				position.y() + bounds.getMinY(),
				position.x() + bounds.getMaxX(),
				position.y() + bounds.getMaxY()
			);
		}

		// Fallback for other shapes
		return new AABB(
			position.x() - 5,
			position.y() - 5,
			position.x() + 5,
			position.y() + 5
		);
	}

	/**
	 * QuadTree implementation for spatial partitioning
	 */
	private class QuadTree {
		private final AABB bounds;
		private final int depth;
		private final Set<ColliderNode> objects = new HashSet<>();
		private QuadTree[] children = null;

		public QuadTree(AABB bounds, int depth) {
			this.bounds = bounds;
			this.depth = depth;
		}

		/**
		 * Inserts a collider into the quadtree
		 */
		public void insert(ColliderNode node) {
			// Skip if invalid or outside bounds
			AABB nodeAABB = getNodeAABB(node);
			if (!isNodeValid(node) || !bounds.intersects(nodeAABB)) {
				return;
			}

			// If we have space or max depth, add here
			if ((objects.size() < MAX_OBJECTS_PER_NODE || depth >= MAX_DEPTH) && children == null) {
				objects.add(node);
				return;
			}

			// Otherwise subdivide if needed
			if (children == null) {
				subdivide();

				// Redistribute existing objects
				Set<ColliderNode> oldObjects = new HashSet<>(objects);
				objects.clear();

				for (ColliderNode existingNode : oldObjects) {
					insertIntoChildren(existingNode);
				}
			}

			// Try to insert into children
			insertIntoChildren(node);
		}

		/**
		 * Inserts a node into child quadtrees
		 */
		private void insertIntoChildren(ColliderNode node) {
			boolean addedToChild = false;
			AABB nodeAABB = getNodeAABB(node);

			for (QuadTree child : children) {
				if (child.bounds.intersects(nodeAABB)) {
					child.insert(node);
					addedToChild = true;
				}
			}

			// If couldn't add to any child, keep at this level
			if (!addedToChild) {
				objects.add(node);
			}
		}

		/**
		 * Subdivides this quadtree into four children
		 */
		private void subdivide() {
			float midX = (bounds.getMinX() + bounds.getMaxX()) * 0.5f;
			float midY = (bounds.getMinY() + bounds.getMaxY()) * 0.5f;

			children = new QuadTree[4];

			// Create the four children
			children[0] = new QuadTree(new AABB(bounds.getMinX(), bounds.getMinY(), midX, midY), depth + 1); // Bottom-left
			children[1] = new QuadTree(new AABB(midX, bounds.getMinY(), bounds.getMaxX(), midY), depth + 1); // Bottom-right
			children[2] = new QuadTree(new AABB(bounds.getMinX(), midY, midX, bounds.getMaxY()), depth + 1); // Top-left
			children[3] = new QuadTree(new AABB(midX, midY, bounds.getMaxX(), bounds.getMaxY()), depth + 1); // Top-right
		}

		/**
		 * Gets potential collision partners for a node
		 */
		public Set<ColliderNode> getPotentialCollisions(ColliderNode node) {
			Set<ColliderNode> result = new HashSet<>();

			// Skip if invalid or outside bounds
			AABB nodeAABB = getNodeAABB(node);
			if (!isNodeValid(node) || !bounds.intersects(nodeAABB)) {
				return result;
			}

			// Add objects at this level (except self)
			for (ColliderNode other : objects) {
				if (other != node) {
					result.add(other);
				}
			}

			// If we have children, check them too
			if (children != null) {
				for (QuadTree child : children) {
					if (child.bounds.intersects(nodeAABB)) {
						result.addAll(child.getPotentialCollisions(node));
					}
				}
			}

			return result;
		}

		/**
		 * Checks if a node is valid
		 */
		private boolean isNodeValid(ColliderNode node) {
			return NodeValidator.isColliderNodeValid(node);
		}
	}

	/**
	 * Gets potential colliders along a ray path.
	 * This optimizes raycasts by only checking relevant colliders.
	 *
	 * @param origin Ray origin
	 * @param direction Ray direction
	 * @param maxDistance Maximum ray distance
	 * @return Set of potential colliders that might intersect with the ray
	 */
	public Set<ColliderNode> getPotentialCollidersAlongRay(Vector2D origin, Vector2D direction, float maxDistance) {
		// Normalize direction
		float magnitude = direction.magnitude();
		if (magnitude < EPSILON) {
			return new HashSet<>();
		}

		Vector2D normalizedDir = direction.scale(1.0f / magnitude);

		// Create a bounding box that encompasses the ray
		Vector2D end = origin.add(normalizedDir.scale(maxDistance));

		float minX = Math.min(origin.x(), end.x());
		float minY = Math.min(origin.y(), end.y());
		float maxX = Math.max(origin.x(), end.x());
		float maxY = Math.max(origin.y(), end.y());

		// Add padding to ensure we catch colliders that are just outside the ray path
		float padding = Math.max(10.0f, maxDistance * 0.01f); // Adjust based on your game's scale

		AABB rayBounds = new AABB(
			minX - padding,
			minY - padding,
			maxX + padding,
			maxY + padding
		);

		return queryBoxForRay(rayBounds, origin, normalizedDir, maxDistance);
	}

	/**
	 * Query the quadtree for a ray with an approximate bounding box.
	 * Then performs a more accurate ray-AABB check on potential colliders.
	 */
	private Set<ColliderNode> queryBoxForRay(AABB rayBounds, Vector2D origin, Vector2D direction, float maxDistance) {
		// First get all colliders in the broad ray bounds
		Set<ColliderNode> potentialColliders = queryBox(rayBounds);
		Set<ColliderNode> result = new HashSet<>();

		for (ColliderNode node : potentialColliders) {
			if (!NodeValidator.isColliderNodeValid(node)) {
				continue;
			}

			// Get AABB for this collider
			AABB colliderAABB = getNodeAABB(node);

			// Perform ray-AABB intersection test
			if (rayIntersectsAABB(origin, direction, maxDistance, colliderAABB)) {
				result.add(node);
			}
		}

		return result;
	}

	/**
	 * Tests if a ray intersects an AABB.
	 * Uses the slab method for intersection testing.
	 */
	// Source: https://tavianator.com/2022/ray_box_boundary.html
	private boolean rayIntersectsAABB(Vector2D origin, Vector2D direction, float maxDistance, AABB aabb) {
		float tMin = Float.NEGATIVE_INFINITY;
		float tMax = Float.POSITIVE_INFINITY;

		// For each axis (X and Y)
		for (int i = 0; i < 2; i++) {
			float d = i == 0 ? direction.x() : direction.y();
			float o = i == 0 ? origin.x() : origin.y();
			float min = i == 0 ? aabb.getMinX() : aabb.getMinY();
			float max = i == 0 ? aabb.getMaxX() : aabb.getMaxY();

			// Ray is parallel to slab
			if (Math.abs(d) < EPSILON) {
				// Ray origin not inside slab
				if (o < min || o > max) {
					return false;
				}
			} else {
				// Calculate intersections with slab planes
				float ood = 1.0f / d;
				float t1 = (min - o) * ood;
				float t2 = (max - o) * ood;

				// Make t1 the closest intersection
				if (t1 > t2) {
					float temp = t1;
					t1 = t2;
					t2 = temp;
				}

				// Update tMin and tMax
				tMin = Math.max(tMin, t1);
				tMax = Math.min(tMax, t2);

				// Exit early if no intersection possible
				if (tMin > tMax) {
					return false;
				}
			}
		}

		// Check if intersection is within ray length
		return tMin <= maxDistance && tMax >= 0;
	}

	/**
	 * Helper method to query the quadtree for all colliders in a given box.
	 * Returns all colliders that intersect with the given AABB.
	 *
	 * @param bounds The AABB bounds to query
	 * @return Set of collider nodes that intersect with the bounds
	 */
	private Set<ColliderNode> queryBox(AABB bounds) {
		// If quadtree hasn't been built yet, return empty set
		if (quadRoot == null) {
			return Collections.emptySet();
		}

		Set<ColliderNode> result = new HashSet<>();
		queryBoxRecursive(quadRoot, bounds, result);
		return result;
	}

	/**
	 * Recursively query a quadtree node for colliders in a given box.
	 *
	 * @param node The quadtree node to query
	 * @param bounds The AABB bounds to check against
	 * @param result The set to collect results in
	 */
	private void queryBoxRecursive(QuadTree node, AABB bounds, Set<ColliderNode> result) {
		// Skip if this node doesn't intersect the query bounds
		if (!node.bounds.intersects(bounds)) {
			return;
		}

		// Add objects at this level that intersect the bounds
		for (ColliderNode collider : node.objects) {
			// Skip invalid nodes
			if (!NodeValidator.isColliderNodeValid(collider)) {
				continue;
			}

			AABB colliderAABB = getNodeAABB(collider);
			if (bounds.intersects(colliderAABB)) {
				result.add(collider);
			}
		}

		// If we have children, check them too
		if (node.children != null) {
			for (QuadTree child : node.children) {
				queryBoxRecursive(child, bounds, result);
			}
		}
	}
}