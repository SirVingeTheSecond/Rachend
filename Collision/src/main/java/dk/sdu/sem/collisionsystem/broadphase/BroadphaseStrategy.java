package dk.sdu.sem.collisionsystem.broadphase;

import dk.sdu.sem.collisionsystem.ColliderNode;
import dk.sdu.sem.collisionsystem.CollisionPair;

import java.util.Set;

/**
 * Strategy interface for broadphase collision detection.
 */
public interface BroadphaseStrategy {
	/**
	 * Finds potential collision pairs from a set of colliders.
	 *
	 * @param colliderNodes Set of collider nodes to check
	 * @return Set of potential collision pairs that need detailed checking
	 */
	Set<CollisionPair> findPotentialCollisions(Set<ColliderNode> colliderNodes);
}
