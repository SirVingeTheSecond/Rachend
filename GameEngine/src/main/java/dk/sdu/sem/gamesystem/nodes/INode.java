package dk.sdu.sem.gamesystem.nodes;

import dk.sdu.sem.gamesystem.components.IComponent;

import java.util.Set;

/**
 * Interface for node types that represent entities with specific component combinations.
 * A node represents a specific combination of components that an entity must have.
 */
public interface INode {
	/**
	 * Gets the set of component types required by this node.
	 *
	 * @return Set of component class objects
	 */
	Set<Class<? extends IComponent>> getRequiredComponents();
}
