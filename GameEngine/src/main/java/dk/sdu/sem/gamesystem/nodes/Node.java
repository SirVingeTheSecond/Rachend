package dk.sdu.sem.gamesystem.nodes;

import dk.sdu.sem.gamesystem.components.IComponent;
import dk.sdu.sem.gamesystem.data.Entity;

import java.util.Set;

/**
 * Interface for node types that represent entities with specific component combinations.
 * A node represents a specific combination of components that an entity must have.
 */
public abstract class Node {
	private Entity entity;

	public void initialize(Entity entity) {
		this.entity = entity;
	}

	/**
	 * Gets the set of component types required by this node.
	 *
	 * @return Set of component class objects
	 */
	public abstract Set<Class<? extends IComponent>> getRequiredComponents();

	/**
	 * Gets the entity of this node.
	 *
	 * @return Entity of this node
	 */
	public Entity getEntity() {
		return entity;
	}
}
