package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.commonsystem.INodeProvider;

/**
 * Provider for TilemapColliderNode instances.
 */
public class TilemapColliderNodeProvider implements INodeProvider<TilemapColliderNode> {
	@Override
	public Class<TilemapColliderNode> getNodeType() {
		return TilemapColliderNode.class;
	}

	@Override
	public TilemapColliderNode create() {
		return new TilemapColliderNode();
	}
}