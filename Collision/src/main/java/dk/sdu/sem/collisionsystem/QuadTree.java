package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.collision.shapes.RectangleShape;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.HashSet;
import java.util.Set;

/**
 * Spatial partitioning structure for efficient collision detection.
 */
public class QuadTree {
	private static final int MAX_ENTITIES = 8;
	private static final int MAX_DEPTH = 5;

	private final AABB bounds;
	private final int depth;
	private Set<ColliderNode> entities;
	private QuadTree[] children;

	public QuadTree(AABB bounds, int depth) {
		this.bounds = bounds;
		this.depth = depth;
		this.entities = new HashSet<>();
		this.children = null;
	}

	public boolean insert(ColliderNode node) {
		// Skip if node is invalid
		if (node == null || node.getEntity() == null || node.collider == null ||
			node.collider.getCollisionShape() == null) {
			return false;
		}

		// Check if entity is within bounds
		AABB entityAABB = getAABBForCollider(node);
		if (!bounds.intersects(entityAABB)) {
			return false;
		}

		// If we haven't subdivided yet and we're not at max capacity or max depth, add to this node
		if (children == null && (entities.size() < MAX_ENTITIES || depth >= MAX_DEPTH)) {
			entities.add(node);
			return true;
		}

		// Subdivide if needed
		if (children == null) {
			subdivide();

			// Redistribute existing entities
			Set<ColliderNode> oldEntities = entities;
			entities = new HashSet<>();

			for (ColliderNode existing : oldEntities) {
				insertIntoChildren(existing);
			}
		}

		// Try to add to children
		return insertIntoChildren(node);
	}

	private void subdivide() {
		AABB[] childBounds = bounds.split();
		children = new QuadTree[4];

		for (int i = 0; i < 4; i++) {
			children[i] = new QuadTree(childBounds[i], depth + 1);
		}
	}

	private boolean insertIntoChildren(ColliderNode node) {
		for (int i = 0; i < 4; i++) {
			if (children[i].insert(node)) {
				return true;
			}
		}

		// If entity spans multiple quadrants, keep it at this level
		entities.add(node);
		return true;
	}

	public Set<ColliderNode> getPotentialCollisions(ColliderNode node) {
		Set<ColliderNode> result = new HashSet<>();

		// Skip if node is invalid
		if (node == null || node.getEntity() == null || node.collider == null ||
			node.collider.getCollisionShape() == null) {
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

	private AABB getAABBForCollider(ColliderNode node) {
		ICollisionShape shape = node.collider.getCollisionShape();
		Vector2D position = node.transform.getPosition().add(node.collider.getOffset());

		if (shape instanceof CircleShape) {
			CircleShape circle = (CircleShape) shape;
			float radius = circle.getRadius();
			return AABB.fromCenterAndSize(position, radius, radius);
		}
		else if (shape instanceof RectangleShape) {
			RectangleShape rect = (RectangleShape) shape;
			return new AABB(
				rect.getPosition().getX(),
				rect.getPosition().getY(),
				rect.getPosition().getX() + rect.getWidth(),
				rect.getPosition().getY() + rect.getHeight()
			);
		}

		// Fallback for other shape types
		return AABB.fromCenterAndSize(position, 1.0f, 1.0f);
	}
}