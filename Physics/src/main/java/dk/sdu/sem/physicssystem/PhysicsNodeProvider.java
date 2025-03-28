package dk.sdu.sem.physicssystem;

import dk.sdu.sem.commonsystem.INodeProvider;

/**
 * Provider for PhysicsNode instances.
 * This class is responsible for creating instances of PhysicsNode when requested
 * by the NodeFactory through the ServiceLoader.
 */
public class PhysicsNodeProvider implements INodeProvider<PhysicsNode> {

	/**
	 * Returns the class of the node this provider creates.
	 * This method is used by the NodeFactory to map node types to their providers.
	 *
	 * @return The PhysicsNode class
	 */
	@Override
	public Class<PhysicsNode> getNodeType() {
		return PhysicsNode.class;
	}

	/**
	 * Creates a new instance of PhysicsNode.
	 * This method is called by the NodeFactory when a new PhysicsNode is needed.
	 *
	 * @return A new PhysicsNode instance
	 */
	@Override
	public PhysicsNode create() {
		return new PhysicsNode();
	}
}