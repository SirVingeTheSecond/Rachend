package dk.sdu.sem.collisionsystem.broadphase;

import dk.sdu.sem.collision.CollisionPair;
import dk.sdu.sem.collisionsystem.ColliderNode;

import java.util.Set;

/**
 * Strategy interface for broadphase collision detection.
 * Implementations of this interface identify potential collision pairs.
 */
public interface BroadphaseStrategy {
	/**
	 * Finds potential collision pairs from a set of colliders.
	 * The returned pairs need further detailed collision tests in the narrowphase.
	 *
	 * @param colliderNodes Set of collider nodes to check
	 * @return Set of potential collision pairs that need detailed checking
	 */
	Set<CollisionPair> findPotentialCollisions(Set<ColliderNode> colliderNodes);
}