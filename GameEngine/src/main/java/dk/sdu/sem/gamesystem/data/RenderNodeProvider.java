package dk.sdu.sem.gamesystem.data;

import dk.sdu.sem.commonsystem.INodeProvider;

/**
 * Provider for RenderNode instances.
 */
public class RenderNodeProvider implements INodeProvider<RenderNode> {
	@Override
	public Class<RenderNode> getNodeType() {
		return RenderNode.class;
	}

	@Override
	public RenderNode create() {
		return new RenderNode();
	}
}