package dk.sdu.sem.collisionsystem.systems;

import dk.sdu.sem.collision.data.CollisionPair;
import dk.sdu.sem.collision.data.TriggerPair;
import dk.sdu.sem.collisionsystem.CollisionLayerMatrix;
import dk.sdu.sem.collisionsystem.broadphase.BroadphaseStrategy;
import dk.sdu.sem.collisionsystem.broadphase.QuadTreeBroadphase;
import dk.sdu.sem.collisionsystem.narrowphase.NarrowPhaseDetector;
import dk.sdu.sem.collisionsystem.nodes.ColliderNode;
import dk.sdu.sem.collisionsystem.state.CollisionState;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;

import java.util.HashSet;
import java.util.Set;

/**
 * System responsible for detecting potential and actual collisions.
 * Handles both broad phase and narrow phase collision detection.
 */
public class CollisionDetectionSystem {
	// Collision state
	private final CollisionState collisionState;

	// Collision detection strategies
	private final BroadphaseStrategy broadphase;
	private final NarrowPhaseDetector narrowphase;

	// Layer collision matrix
	private final CollisionLayerMatrix layerMatrix;

	/**
	 * Creates a new collision detection system.
	 *
	 * @param collisionState The shared collision state
	 */
	public CollisionDetectionSystem(CollisionState collisionState) {
		this.collisionState = collisionState;
		this.broadphase = new QuadTreeBroadphase();
		this.layerMatrix = new CollisionLayerMatrix();
		this.narrowphase = new NarrowPhaseDetector(layerMatrix);
	}

	/**
	 * Processes collision detection for the current frame.
	 */
	public void process() {
		// Get all collider nodes
		Set<ColliderNode> colliderNodes = NodeManager.active().getNodes(ColliderNode.class);

		// Skip if no colliders
		if (colliderNodes.size() < 2) {
			collisionState.setCurrentCollisions(new HashSet<>());
			collisionState.setCurrentTriggers(new HashSet<>());
			return;
		}

		// STEP 1: Broadphase - Find potential collision pairs
		Set<CollisionPair> potentialCollisions = broadphase.findPotentialCollisions(colliderNodes);

		// STEP 2: Narrowphase - Determine actual collisions with contact info
		Set<CollisionPair> allCollisions = narrowphase.detectCollisions(potentialCollisions);

		// STEP 3: Separate physical collisions from triggers
		Set<CollisionPair> physicalCollisions = new HashSet<>();
		Set<TriggerPair> triggerCollisions = new HashSet<>();

		for (CollisionPair pair : allCollisions) {
			if (pair.isTrigger()) {
				triggerCollisions.add(new TriggerPair(pair));
			} else {
				physicalCollisions.add(pair);
			}
		}

		// STEP 4: Update collision state
		collisionState.setCurrentCollisions(physicalCollisions);
		collisionState.setCurrentTriggers(triggerCollisions);
	}

	/**
	 * Cleans up collision state for a destroyed entity.
	 *
	 * @param entity The entity to clean up
	 */
	public void cleanupEntity(Entity entity) {
		// Nothing specific to clean up here
		// The collision state is reset each frame
	}
}
