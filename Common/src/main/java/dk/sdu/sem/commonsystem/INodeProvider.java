package dk.sdu.sem.commonsystem;

/**
 * Interface for node providers. Each node type should have a corresponding provider.
 * This allows creation of nodes without using reflection.
 *
 * @param <T> The type of node this provider creates
 */
public interface INodeProvider<T extends Node> {
	/**
	 * Returns the class of the node this provider creates.
	 */
	Class<T> getNodeType();

	/**
	 * Creates a new instance of the node.
	 */
	T create();
}