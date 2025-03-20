package dk.sdu.sem.gamesystem.data;

import dk.sdu.sem.commonsystem.INodeProvider;

public class TileMapNodeProvider implements INodeProvider<TileMapNode> {
	@Override
	public Class<TileMapNode> getNodeType() {
		return TileMapNode.class;
	}

	@Override
	public TileMapNode create() {
		return new TileMapNode();
	}
}