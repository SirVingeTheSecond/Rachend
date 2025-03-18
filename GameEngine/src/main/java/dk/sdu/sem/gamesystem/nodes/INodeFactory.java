package dk.sdu.sem.gamesystem.nodes;

import dk.sdu.sem.gamesystem.data.Entity;

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
	<T extends Node> T createNode(Class<T> nodeClass, Entity entity);

	/**
	 * Gets a cached instance of the specified node type or creates a new one if not cached.
	 *
	 * @param nodeClass The class of node to get
	 * @param <T> The node type
	 * @return An instance of the specified node type
	 * @throws IllegalArgumentException if node creation fails
	 */
	<T extends Node> T getOrCreateNode(Class<T> nodeClass, Entity entity);

	/**
	 * Clears any cached node instances.
	 */
	void clearCache();
}