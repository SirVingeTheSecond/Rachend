package dk.sdu.sem.collisionsystem.broadphase;

import dk.sdu.sem.collision.data.AABB;
import dk.sdu.sem.collision.data.CollisionPair;
import dk.sdu.sem.collision.shapes.BoxShape;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.collisionsystem.nodes.ColliderNode;
import dk.sdu.sem.collisionsystem.utils.NodeValidator;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Broadphase strategy using a QuadTree for spatial partitioning.
 * Efficiently finds potential collision pairs without detailed collision checks.
 */
public class QuadTreeBroadphase implements BroadphaseStrategy {
	private static final int MAX_DEPTH = 5;
	private static final int MAX_OBJECTS_PER_NODE = 10;
	private static final float DEFAULT_WORLD_SIZE = 2000.0f;
	private static final boolean USE_DYNAMIC_BOUNDS = true;

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

		QuadTree quadTree = new QuadTree(worldBounds, 0);

		// Insert all nodes into quadtree
		for (ColliderNode node : validNodes) {
			quadTree.insert(node);
		}

		// For each node, find potential collision partners
		for (ColliderNode nodeA : validNodes) {
			// Skip if node is invalid
			if (!NodeValidator.isColliderNodeValid(nodeA)) {
				continue;
			}

			// Get potential collision partners
			Set<ColliderNode> potentialPartners = quadTree.getPotentialCollisions(nodeA);

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

		/**
		 * Gets an AABB for a collider node
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
			}

			// Fallback for other shapes
			return new AABB(
				position.x() - 5,
				position.y() - 5,
				position.x() + 5,
				position.y() + 5
			);
		}
	}
}