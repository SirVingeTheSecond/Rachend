package dk.sdu.sem.collisionsystem.broadphase;

import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.collision.shapes.RectangleShape;
import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.collisionsystem.AABB;
import dk.sdu.sem.collisionsystem.ColliderNode;
import dk.sdu.sem.collisionsystem.CollisionPair;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Quadtree implementation of the broadphase strategy.
 */
public class QuadTreeBroadphase implements BroadphaseStrategy {
	private static final int MAX_DEPTH = 5;
	private static final int MAX_OBJECTS_PER_NODE = 8;

	@Override
	public Set<CollisionPair> findPotentialCollisions(Set<ColliderNode> colliderNodes) {
		Set<CollisionPair> potentialCollisions = new HashSet<>();

		// Skip processing if there are fewer than 2 colliders
		if (colliderNodes.size() < 2) {
			return potentialCollisions;
		}

		// Filter out invalid nodes before processing
		List<ColliderNode> validNodes = new ArrayList<>();
		for (ColliderNode node : colliderNodes) {
			if (isNodeValid(node)) {
				validNodes.add(node);
			}
		}

		// Create quadtree and insert all valid colliders
		QuadTree quadTree = new QuadTree(getWorldBounds(validNodes), 0);
		for (ColliderNode node : validNodes) {
			quadTree.insert(node);
		}

		// Check each collider against potential collision partners
		for (ColliderNode nodeA : validNodes) {
			// Get potential collisions from quadtree
			Set<ColliderNode> potentialPartners = quadTree.getPotentialCollisions(nodeA);

			for (ColliderNode nodeB : potentialPartners) {
				// Skip self-collision
				if (nodeB == nodeA) {
					continue;
				}

				// Add as potential collision pair
				potentialCollisions.add(new CollisionPair(nodeA, nodeB, null, false));
			}
		}

		return potentialCollisions;
	}

	/**
	 * Estimates the world bounds based on entity positions.
	 */
	private AABB getWorldBounds(List<ColliderNode> colliders) {
		float minX = -1000, minY = -1000, maxX = 1000, maxY = 1000;

		// If we have entities, compute actual bounds
		if (!colliders.isEmpty()) {
			// Start with first entity's position
			ColliderNode first = colliders.get(0);
			Vector2D pos = first.transform.getPosition();
			minX = pos.getX() - 500;
			minY = pos.getY() - 500;
			maxX = pos.getX() + 500;
			maxY = pos.getY() + 500;

			// Expand bounds to include all entities with some padding
			for (ColliderNode node : colliders) {
				Vector2D position = node.transform.getPosition();
				float radius = getColliderRadius(node);

				minX = Math.min(minX, position.getX() - radius - 100);
				minY = Math.min(minY, position.getY() - radius - 100);
				maxX = Math.max(maxX, position.getX() + radius + 100);
				maxY = Math.max(maxY, position.getY() + radius + 100);
			}
		}

		return new AABB(minX, minY, maxX, maxY);
	}

	/**
	 * Gets the radius of a collider for bounds calculation.
	 */
	private float getColliderRadius(ColliderNode node) {
		ICollisionShape shape = node.collider.getCollisionShape();
		if (shape instanceof CircleShape) {
			return ((CircleShape) shape).getRadius();
		} else if (shape instanceof RectangleShape) {
			RectangleShape rect = (RectangleShape) shape;
			return Math.max(rect.getWidth(), rect.getHeight()) / 2;
		}
		return 10; // Default size if unknown shape
	}

	/**
	 * Checks if a node is valid for collision detection.
	 */
	private boolean isNodeValid(ColliderNode node) {
		return node != null &&
			node.getEntity() != null &&
			node.getEntity().getScene() != null &&
			node.transform != null &&
			node.collider != null &&
			node.collider.getCollisionShape() != null;
	}

	/**
	 * Quadtree node for spatial partitioning.
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
			// Skip if this node is invalid
			if (node == null || node.getEntity() == null || node.transform == null ||
				node.collider == null || node.collider.getCollisionShape() == null) {
				return false;
			}

			// Check if entity is within bounds
			AABB entityAABB = getAABBForCollider(node);
			if (!bounds.intersects(entityAABB)) {
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

				// Redistribute existing entities
				Set<ColliderNode> oldEntities = new HashSet<>(entities);
				entities.clear();

				for (ColliderNode existing : oldEntities) {
					insertIntoChildren(existing);
				}
			}

			// Try to add to children
			return insertIntoChildren(node);
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
		 */
		private boolean insertIntoChildren(ColliderNode node) {
			boolean addedToChild = false;

			for (int i = 0; i < 4; i++) {
				if (children[i].insert(node)) {
					addedToChild = true;
				}
			}

			// If entity couldn't be added to any children or spans multiple quadrants,
			// keep it at this level
			if (!addedToChild) {
				entities.add(node);
				return true;
			}

			return true;
		}

		/**
		 * Gets all potential collision partners for a collider.
		 */
		public Set<ColliderNode> getPotentialCollisions(ColliderNode node) {
			Set<ColliderNode> result = new HashSet<>();

			// Skip if node is invalid
			if (node == null || node.getEntity() == null || node.transform == null ||
				node.collider == null || node.collider.getCollisionShape() == null) {
				return result;
			}

			// Check if entity is within bounds
			AABB entityAABB = getAABBForCollider(node);
			if (!bounds.intersects(entityAABB)) {
				return result;
			}

			// Add entities at this level
			for (ColliderNode other : entities) {
				if (other != node) {
					result.add(other);
				}
			}

			// If we have children, collect from them too
			if (children != null) {
				for (int i = 0; i < 4; i++) {
					if (children[i].bounds.intersects(entityAABB)) {
						result.addAll(children[i].getPotentialCollisions(node));
					}
				}
			}

			return result;
		}

		/**
		 * Gets an AABB for a collider, based on its shape.
		 */
		private AABB getAABBForCollider(ColliderNode node) {
			ICollisionShape shape = node.collider.getCollisionShape();
			Vector2D position = node.transform.getPosition().add(node.collider.getOffset());

			if (shape instanceof CircleShape) {
				CircleShape circle = (CircleShape) shape;
				float radius = circle.getRadius();
				return new AABB(
					position.getX() - radius,
					position.getY() - radius,
					position.getX() + radius,
					position.getY() + radius
				);
			}
			else if (shape instanceof RectangleShape) {
				RectangleShape rect = (RectangleShape) shape;
				return new AABB(
					position.getX() - rect.getWidth()/2,
					position.getY() - rect.getHeight()/2,
					position.getX() + rect.getWidth()/2,
					position.getY() + rect.getHeight()/2
				);
			}

			// Fallback for other shape types - small AABB around position
			return new AABB(
				position.getX() - 1.0f,
				position.getY() - 1.0f,
				position.getX() + 1.0f,
				position.getY() + 1.0f
			);
		}
	}
}