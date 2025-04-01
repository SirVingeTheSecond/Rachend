package dk.sdu.sem.gamesystem.data;

import dk.sdu.sem.commonsystem.INodeProvider;

/**
 * Provider for TilemapNode instances.
 */
public class TilemapNodeProvider implements INodeProvider<TilemapNode> {
	@Override
	public Class<TilemapNode> getNodeType() {
		return TilemapNode.class;
	}

	@Override
	public TilemapNode create() {
		return new TilemapNode();
	}
}