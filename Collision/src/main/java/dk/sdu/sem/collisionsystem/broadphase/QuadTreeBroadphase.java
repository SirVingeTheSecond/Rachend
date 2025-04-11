package dk.sdu.sem.collisionsystem.broadphase;

import dk.sdu.sem.collision.CollisionPair;
import dk.sdu.sem.collision.shapes.BoxShape;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.collisionsystem.AABB;
import dk.sdu.sem.collisionsystem.ColliderNode;
import dk.sdu.sem.collisionsystem.utils.NodeValidator;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Quadtree implementation of the broadphase collision detection strategy.
 * Uses spatial partitioning to find potential collision pairs.
 */
public class QuadTreeBroadphase implements BroadphaseStrategy {
	private static final int MAX_DEPTH = 5;
	private static final int MAX_OBJECTS_PER_NODE = 8;
	private static final float DEFAULT_PADDING = 100.0f;
	private static final float DEFAULT_WORLD_SIZE = 1000.0f;

	@Override
	public Set<CollisionPair> findPotentialCollisions(Set<ColliderNode> colliderNodes) {
		Set<CollisionPair> potentialCollisions = new HashSet<>();

		// Skip processing if there are fewer than 2 colliders
		if (colliderNodes.size() < 2) {
			return potentialCollisions;
		}

		// Filter out invalid nodes before processing
		List<ColliderNode> validNodes = colliderNodes.stream()
			.filter(NodeValidator::isColliderNodeValid)
			.collect(Collectors.toList());

		if (validNodes.size() < 2) {
			return potentialCollisions;
		}

		// Create quadtree and insert all valid colliders
		QuadTree quadTree = new QuadTree(getWorldBounds(validNodes), 0);
		validNodes.forEach(quadTree::insert);

		// Check each collider against potential collision partners
		for (ColliderNode nodeA : validNodes) {
			// Get potential collisions from quadtree
			Set<ColliderNode> potentialPartners = quadTree.getPotentialCollisions(nodeA);
			for (ColliderNode nodeB : potentialPartners) {
				// Skip self-collision
				if (nodeB == nodeA) {
					continue;
				}

				// Add as potential collision pair with proper parameters
				addPotentialCollisionPair(potentialCollisions, nodeA, nodeB);
			}
		}

		return potentialCollisions;
	}

	/**
	 * Adds a potential collision pair to the set
	 */
	private void addPotentialCollisionPair(Set<CollisionPair> collisions, ColliderNode nodeA, ColliderNode nodeB) {
		boolean isTrigger = nodeA.collider.isTrigger() || nodeB.collider.isTrigger();

		collisions.add(new CollisionPair(
			nodeA.getEntity(),
			nodeB.getEntity(),
			nodeA.collider,
			nodeB.collider,
			null, // null contact point since this is just potential collision
			isTrigger
		));
	}

	/**
	 * Estimates the world bounds based on entity positions.
	 */
	private AABB getWorldBounds(List<ColliderNode> colliders) {
		// If no colliders, return a default world size
		if (colliders.isEmpty()) {
			return new AABB(
				-DEFAULT_WORLD_SIZE,
				-DEFAULT_WORLD_SIZE,
				DEFAULT_WORLD_SIZE,
				DEFAULT_WORLD_SIZE
			);
		}

		// Start with first entity's position
		ColliderNode first = colliders.get(0);
		Vector2D pos = first.transform.getPosition();
		float radius = getColliderRadius(first);

		float minX = pos.x() - radius - DEFAULT_PADDING;
		float minY = pos.y() - radius - DEFAULT_PADDING;
		float maxX = pos.x() + radius + DEFAULT_PADDING;
		float maxY = pos.y() + radius + DEFAULT_PADDING;

		// Expand bounds to include all entities with padding
		for (int i = 1; i < colliders.size(); i++) {
			ColliderNode node = colliders.get(i);
			Vector2D position = node.transform.getPosition();
			radius = getColliderRadius(node);

			minX = Math.min(minX, position.x() - radius - DEFAULT_PADDING);
			minY = Math.min(minY, position.y() - radius - DEFAULT_PADDING);
			maxX = Math.max(maxX, position.x() + radius + DEFAULT_PADDING);
			maxY = Math.max(maxY, position.y() + radius + DEFAULT_PADDING);
		}

		return new AABB(minX, minY, maxX, maxY);
	}

	/**
	 * Gets the radius of a collider for bounds calculation.
	 */
	private float getColliderRadius(ColliderNode node) {
		ICollisionShape shape = node.collider.getShape();
		if (shape instanceof CircleShape circle) {
			return circle.getRadius();
		} else if (shape instanceof BoxShape box) {
			return Math.max(box.getWidth(), box.getHeight()) / 2;
		}
		return 10; // Default size if unknown shape
	}

	/**
	 * Quadtree node for spatial partitioning.
	 * Recursively divides space into four quadrants to efficiently store and query objects.
	 */
	private class QuadTree {
		private final AABB bounds;
		private final int depth;
		private final Set<ColliderNode> entities = new HashSet<>();
		private QuadTree[] children = null;

		public QuadTree(AABB bounds, int depth) {
			this.bounds = bounds;
			this.depth = depth;
		}

		/**
		 * Inserts a collider into the quadtree.
		 */
		public boolean insert(ColliderNode node) {
			// Skip if node is invalid or outside bounds
			if (!isWithinBounds(node)) {
				return false;
			}

			// If we haven't subdivided yet and have space, add to this node
			if (children == null && (entities.size() < MAX_OBJECTS_PER_NODE || depth >= MAX_DEPTH)) {
				entities.add(node);
				return true;
			}

			// Subdivide if needed
			if (children == null) {
				subdivide();

				// Redistribute existing entities into children
				Set<ColliderNode> oldEntities = new HashSet<>(entities);
				entities.clear();

				for (ColliderNode existing : oldEntities) {
					insertIntoChildren(existing);
				}
			}

			// Try to add to children (or keep at this level if it spans multiple quadrants)
			insertIntoChildren(node);
			return true;
		}

		/**
		 * Creates four child quadtrees.
		 */
		private void subdivide() {
			AABB[] childBounds = bounds.split();
			children = new QuadTree[4];

			for (int i = 0; i < 4; i++) {
				children[i] = new QuadTree(childBounds[i], depth + 1);
			}
		}

		/**
		 * Tries to insert a node into child quadtrees.
		 * If it doesn't fit in any child, keeps it at the current level.
		 */
		private void insertIntoChildren(ColliderNode node) {
			boolean addedToChild = false;

			for (QuadTree child : children) {
				if (child.insert(node)) {
					addedToChild = true;
				}
			}

			// If node couldn't be added to any child (or spans multiple quadrants),
			// keep it at this level
			if (!addedToChild) {
				entities.add(node);
			}
		}

		/**
		 * Gets all potential collision partners for a collider.
		 */
		public Set<ColliderNode> getPotentialCollisions(ColliderNode node) {
			Set<ColliderNode> result = new HashSet<>();

			// Skip if node is invalid or not within bounds
			if (!isWithinBounds(node)) {
				return result;
			}

			// Add entities at this level
			for (ColliderNode other : entities) {
				if (other != node) {
					result.add(other);
				}
			}

			// If we have children, collect potential collisions from them too
			if (children != null) {
				AABB nodeAABB = getAABBForCollider(node);

				for (QuadTree child : children) {
					if (child.bounds.intersects(nodeAABB)) {
						result.addAll(child.getPotentialCollisions(node));
					}
				}
			}

			return result;
		}

		/**
		 * Helper method to check if a node is within the bounds of this quadtree node.
		 */
		private boolean isWithinBounds(ColliderNode node) {
			return NodeValidator.isColliderNodeValid(node) &&
				bounds.intersects(getAABBForCollider(node));
		}

		/**
		 * Gets an AABB for a collider, based on its shape.
		 */
		private AABB getAABBForCollider(ColliderNode node) {
			ICollisionShape shape = node.collider.getShape();
			Vector2D position = node.transform.getPosition().add(node.collider.getOffset());

			if (shape instanceof CircleShape circle) {
				float radius = circle.getRadius();
				return new AABB(
					position.x() - radius,
					position.y() - radius,
					position.x() + radius,
					position.y() + radius
				);
			} else if (shape instanceof BoxShape box) {
				return new AABB(
					position.x() - box.getWidth() / 2,
					position.y() - box.getHeight() / 2,
					position.x() + box.getWidth() / 2,
					position.y() + box.getHeight() / 2
				);
			}

			// Fallback for other shape types – small AABB around the position
			return new AABB(
				position.x() - 1.0f,
				position.y() - 1.0f,
				position.x() + 1.0f,
				position.y() + 1.0f
			);
		}
	}
}