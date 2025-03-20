package dk.sdu.sem.gamesystem.data;

import dk.sdu.sem.commonsystem.INodeProvider;

/**
 * Provider for RenderNode instances.
 * This class is responsible for creating instances of RenderNode when requested
 * by the NodeFactory through the ServiceLoader.
 */
public class RenderNodeProvider implements INodeProvider<RenderNode> {

	/**
	 * Returns the class of the node this provider creates.
	 * This method is used by the NodeFactory to map node types to their providers.
	 *
	 * @return The RenderNode class
	 */
	@Override
	public Class<RenderNode> getNodeType() {
		return RenderNode.class;
	}

	/**
	 * Creates a new instance of RenderNode.
	 * This method is called by the NodeFactory when a new RenderNode is needed.
	 *
	 * @return A new Render instance
	 */
	@Override
	public RenderNode create() {
		return new RenderNode();
	}
}