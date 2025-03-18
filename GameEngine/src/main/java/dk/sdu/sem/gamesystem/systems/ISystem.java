package dk.sdu.sem.gamesystem.systems;

import dk.sdu.sem.gamesystem.data.Entity;
import dk.sdu.sem.gamesystem.data.Scene;
import dk.sdu.sem.gamesystem.nodes.Node;

import java.util.Set;

/**
 * Interface for all systems that processes on nodes
 * @param <T> The type of node the system processes on
 */
public interface ISystem<T extends Node> {
	/**
	 * Get the node type this system processes on
	 */
	Class<T> getNodeType();

	/**
	 * Process all entities that match the system's node type
	 * @param scene The scene containing the entities
	 */
	default void process(Scene scene) {
		// All entities that match this system's node type
		Set<Entity> entities = scene.getNodeManager().getNodeEntities(getNodeType());

		// Process entities
		for (Entity entity : entities) {
			T node = scene.getNodeManager().createNodeForEntity(getNodeType(), entity);
			if (node != null) {
				processEntity(node);
			}
		}
	}

	/**
	 * Process a single entity node
	 * @param node The node instance for the entity
	 */
	void processEntity(T node);
}