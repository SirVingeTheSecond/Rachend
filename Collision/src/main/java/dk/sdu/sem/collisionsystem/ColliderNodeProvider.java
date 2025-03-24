package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.commonsystem.INodeProvider;

/**
 * Provider for ColliderNode instances.
 */
public class ColliderNodeProvider implements INodeProvider<ColliderNode> {
	@Override
	public Class<ColliderNode> getNodeType() {
		return ColliderNode.class;
	}

	@Override
	public ColliderNode create() {
		return new ColliderNode();
	}
}