package dk.sdu.sem.collisionsystem.narrowphase;

import dk.sdu.sem.collision.*;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.collisionsystem.LayerCollisionMatrix;
import dk.sdu.sem.collisionsystem.narrowphase.solvers.ShapeSolverFactory;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles narrowphase collision detection between any type of colliders.
 * Uses the unified shape solver approach.
 */
public class NarrowPhaseDetector {
	private final LayerCollisionMatrix layerMatrix;

	public NarrowPhaseDetector(LayerCollisionMatrix layerMatrix) {
		this.layerMatrix = layerMatrix;
	}

	/**
	 * Performs detailed collision detection on potential pairs.
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
	 * Checks if two colliders are colliding.
	 */
	public ContactPoint checkCollision(ColliderComponent colliderA, ColliderComponent colliderB) {
		// Skip if either collider is disabled
		if (!colliderA.isEnabled() || !colliderB.isEnabled()) {
			return null;
		}

		// Skip if layers can't collide
		if (!canLayersCollide(colliderA.getLayer(), colliderB.getLayer())) {
			return null;
		}

		// Perform collision test
		return testShapeCollision(
			colliderA.getShape(),
			colliderA.getWorldPosition(),
			colliderB.getShape(),
			colliderB.getWorldPosition()
		);
	}

	/**
	 * Validates if a proposed position is valid for a collider.
	 */
	public boolean isPositionValid(ColliderComponent collider, Vector2D proposedPos) {
		// A valid position is one that doesn't collide with any obstacle
		// A full implementation would need to:
		// 1. Create a ghost collider at the proposed position
		// 2. Check for collisions with all obstacles
		// 3. Return true only if no collisions detected

		return true; // TODO: Implement full validation
	}

	/**
	 * Checks if two layers can collide according to layer matrix.
	 */
	private boolean canLayersCollide(PhysicsLayer layerA, PhysicsLayer layerB) {
		return layerMatrix.canLayersCollide(layerA, layerB);
	}
}