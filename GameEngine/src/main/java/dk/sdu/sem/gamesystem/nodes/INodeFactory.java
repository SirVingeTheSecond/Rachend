package dk.sdu.sem.gamesystem.nodes;

/**
 * Interface for node factory that is responsible for creating node instances.
 */
// We could introduce more specific node factories if needed
public interface INodeFactory {
	/**
	 * Creates a new instance of the specified node type.
	 *
	 * @param nodeClass The class of node to create
	 * @param <T> The node type
	 * @return A new instance of the specified node type
	 * @throws IllegalArgumentException if node creation fails
	 */
	<T extends INode> T createNode(Class<T> nodeClass);

	/**
	 * Gets a cached instance of the specified node type or creates a new one if not cached.
	 *
	 * @param nodeClass The class of node to get
	 * @param <T> The node type
	 * @return An instance of the specified node type
	 * @throws IllegalArgumentException if node creation fails
	 */
	<T extends INode> T getOrCreateNode(Class<T> nodeClass);

	/**
	 * Clears any cached node instances.
	 */
	void clearCache();
}