package dk.sdu.sem.enemysystem;

import dk.sdu.sem.commonsystem.INodeProvider;

/**
 * Provider for EnemyNode instances.
 * This class is responsible for creating instances of EnemyNode when requested
 * by the NodeFactory through the ServiceLoader mechanism.
 */
public class EnemyNodeProvider implements INodeProvider<EnemyNode> {

	/**
	 * Returns the class of the node this provider creates.
	 * This method is used by the NodeFactory to map node types to their providers.
	 *
	 * @return The EnemyNode class
	 */
	@Override
	public Class<EnemyNode> getNodeType() {
		return EnemyNode.class;
	}

	/**
	 * Creates a new instance of EnemyNode.
	 * This method is called by the NodeFactory when a new EnemyNode is needed.
	 *
	 * @return A new EnemyNode instance
	 */
	@Override
	public EnemyNode create() {
		return new EnemyNode();
	}
}
