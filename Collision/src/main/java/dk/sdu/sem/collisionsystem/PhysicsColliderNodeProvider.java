package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.commonsystem.INodeProvider;

/**
 * Provider for PhysicsColliderNode instances.
 */
public class PhysicsColliderNodeProvider implements INodeProvider<PhysicsColliderNode> {
	@Override
	public Class<PhysicsColliderNode> getNodeType() {
		return PhysicsColliderNode.class;
	}

	@Override
	public PhysicsColliderNode create() {
		return new PhysicsColliderNode();
	}
}