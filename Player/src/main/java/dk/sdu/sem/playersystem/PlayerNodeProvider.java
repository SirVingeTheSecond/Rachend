package dk.sdu.sem.playersystem;

import dk.sdu.sem.commonsystem.INodeProvider;

/**
 * Provider for PlayerNode instances.
 * This class is responsible for creating instances of PlayerNode when requested
 * by the NodeFactory through the ServiceLoader mechanism.
 */
public class PlayerNodeProvider implements INodeProvider<PlayerNode> {

	/**
	 * Returns the class of the node this provider creates.
	 * This method is used by the NodeFactory to map node types to their providers.
	 *
	 * @return The PlayerNode class
	 */
	@Override
	public Class<PlayerNode> getNodeType() {
		return PlayerNode.class;
	}

	/**
	 * Creates a new instance of PlayerNode.
	 * This method is called by the NodeFactory when a new PlayerNode is needed.
	 *
	 * @return A new PlayerNode instance
	 */
	@Override
	public PlayerNode create() {
		return new PlayerNode();
	}
}
