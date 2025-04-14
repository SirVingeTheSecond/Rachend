package dk.sdu.sem.collisionsystem.narrowphase;

import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.data.CollisionPair;
import dk.sdu.sem.collision.data.ContactPoint;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.collisionsystem.LayerCollisionMatrix;
import dk.sdu.sem.collisionsystem.narrowphase.solvers.ShapeSolverFactory;
import dk.sdu.sem.collisionsystem.nodes.ColliderNode;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles narrowphase collision detection between any type of colliders.
 */
public class NarrowPhaseDetector {
	private final LayerCollisionMatrix layerMatrix;

	public NarrowPhaseDetector(LayerCollisionMatrix layerMatrix) {
		this.layerMatrix = layerMatrix;
	}

	/**
	 * Performs collision detection on potential pairs.
	 */
	public Set<CollisionPair> detectCollisions(Set<CollisionPair> potentialPairs) {
		Set<CollisionPair> confirmedCollisions = new HashSet<>();

		for (CollisionPair pair : potentialPairs) {
			// Skip if layers can't collide
			if (!canLayersCollide(pair.getColliderA().getLayer(), pair.getColliderB().getLayer())) {
				continue;
			}

			// Perform shape-based collision test
			ContactPoint contact = testShapeCollision(
				pair.getColliderA().getShape(),
				pair.getColliderA().getWorldPosition(),
				pair.getColliderB().getShape(),
				pair.getColliderB().getWorldPosition()
			);

			// If collision detected, create confirmed pair with contact info
			if (contact != null) {
				confirmedCollisions.add(new CollisionPair(
					pair.getEntityA(),
					pair.getEntityB(),
					pair.getColliderA(),
					pair.getColliderB(),
					contact,
					pair.isTrigger()
				));
			}
		}

		return confirmedCollisions;
	}

	/**
	 * Tests collision between two shapes using the shape solver factory.
	 */
	public ContactPoint testShapeCollision(
		ICollisionShape shapeA, Vector2D posA,
		ICollisionShape shapeB, Vector2D posB) {

		return ShapeSolverFactory.solve(shapeA, posA, shapeB, posB);
	}

	/**
	 * Validates if a proposed position for a collider would cause collisions.
	 * Uses a "ghost" collider by temporarily moving the collider to check.
	 *
	 * @param collider The collider to check
	 * @param proposedPos The proposed world position to validate
	 * @return true if the position is valid (no collisions), false otherwise
	 */
	public boolean isPositionValid(ColliderComponent collider, Vector2D proposedPos) {
		if (collider == null || !collider.isEnabled()) {
			return true;
		}

		Entity entity = collider.getEntity();
		TransformComponent transform = entity.getComponent(TransformComponent.class);

		if (transform == null) {
			return true; // No transform, no movement
		}

		// Store original positions
		Vector2D originalEntityPos = transform.getPosition();
		Vector2D offset = collider.getOffset();

		try {
			// Calculate and set temporary position for collision check
			// We need to set the entity position such that the collider ends up at proposedPos
			Vector2D entityProposedPos = proposedPos.subtract(offset);
			transform.setPosition(entityProposedPos);

			// Get all potential colliders to test against
			Set<ColliderNode> colliderNodes = NodeManager.active().getNodes(ColliderNode.class);
			for (ColliderNode node : colliderNodes) {
				ColliderComponent otherCollider = node.collider;

				// Skip invalid colliders
				if (otherCollider == null || !otherCollider.isEnabled()) {
					continue;
				}

				// Skip self-collision
				if (otherCollider == collider || otherCollider.getEntity() == entity) {
					continue;
				}

				// Skip if layers don't interact
				if (!layerMatrix.canLayersCollide(collider.getLayer(), otherCollider.getLayer())) {
					continue;
				}

				// Skip triggers during movement validation (unless configurable)
				if (otherCollider.isTrigger()) {
					continue;
				}

				// Use existing collision detection
				ContactPoint contact = checkCollision(collider, otherCollider);
				if (contact != null) {
					return false; // Collision detected - position is invalid
				}
			}

			// No collisions detected
			return true;
		} finally {
			// Always restore original position, regardless of result
			transform.setPosition(originalEntityPos);
		}
	}

	/**
	 * Internal helper method to check collision between two colliders.
	 */
	// Check if this is redundant code - is this done elsewhere?
	public ContactPoint checkCollision(ColliderComponent colliderA, ColliderComponent colliderB) {
		// Get the shapes
		ICollisionShape shapeA = colliderA.getShape();
		ICollisionShape shapeB = colliderB.getShape();

		// Get world positions
		Vector2D posA = colliderA.getWorldPosition();
		Vector2D posB = colliderB.getWorldPosition();

		// Use existing shape-based collision detection
		return ShapeSolverFactory.solve(shapeA, posA, shapeB, posB);
	}

	/**
	 * Checks if two layers can collide according to layer matrix.
	 */
	private boolean canLayersCollide(PhysicsLayer layerA, PhysicsLayer layerB) {
		return layerMatrix.canLayersCollide(layerA, layerB);
	}
}