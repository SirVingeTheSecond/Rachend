package dk.sdu.sem.gamesystem.data;

import dk.sdu.sem.commonsystem.INodeProvider;

public class SpriteNodeProvider implements INodeProvider<SpriteNode> {
	@Override
	public Class<SpriteNode> getNodeType() {
		return SpriteNode.class;
	}

	@Override
	public SpriteNode create() {
		return new SpriteNode();
	}
}