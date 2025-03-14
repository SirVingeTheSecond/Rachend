package dk.sdu.sem.gamesystem.nodes;

import dk.sdu.sem.gamesystem.components.IComponent;
import dk.sdu.sem.gamesystem.data.Entity;
import dk.sdu.sem.gamesystem.scene.Scene; // Adjust the package as needed

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NodeManager {
	// Map of node type to collections of entities that match the node.
	private Map<Class<? extends INode>, Set<Entity>> nodeCollections = new ConcurrentHashMap<>();

	// Map of entities to the node types (memberships) they belong to.
	private Map<Entity, Set<Class<? extends INode>>> entityNodes = new ConcurrentHashMap<>();

	// Cache of required components for each node type.
	private Map<Class<? extends INode>, Set<Class<? extends IComponent>>> nodeRequirements = new ConcurrentHashMap<>();

	private INodeFactory nodeFactory;

	/**
	 * Constructs a new NodeManager with the specified NodeFactory.
	 *
	 * @param nodeFactory the factory used to obtain or create node instances; must not be null.
	 */
	public NodeManager(INodeFactory nodeFactory) {
		this.nodeFactory = Objects.requireNonNull(nodeFactory, "NodeFactory cannot be null");
	}

	/**
	 * Registers a node type for tracking entities.
	 * <p>
	 * If the node type is not already registered, its required components are cached
	 * using the NodeFactory and an empty, thread-safe collection is created for entities.
	 *
	 * @param nodeClass the class representing the node type to register; must not be null.
	 * @param <T>       the type of the node.
	 */
	public <T extends INode> void registerNodeType(Class<T> nodeClass) {
		nodeCollections.computeIfAbsent(nodeClass, cls -> {
			cacheNodeRequirements(cls);
			return ConcurrentHashMap.newKeySet();
		});
	}

	/**
	 * Caches the required components for the specified node type.
	 * <p>
	 * Obtains a node instance (using getOrCreateNode from the NodeFactory) and then
	 * retrieves its required components. The requirements are stored in the nodeRequirements map.
	 *
	 * @param nodeClass the class representing the node type for which to cache requirements; must not be null.
	 * @param <T>       the type of the node.
	 */
	private <T extends INode> void cacheNodeRequirements(Class<T> nodeClass) {
		T node = nodeFactory.getOrCreateNode(nodeClass);
		Set<Class<? extends IComponent>> requirements = node.getRequiredComponents();
		nodeRequirements.put(nodeClass, requirements);
	}

	/**
	 * Retrieves the collections of entities that belong to the specified node type.
	 * <p>
	 * If the node type is not registered, an empty set is returned.
	 *
	 * @param nodeClass the class representing the node type; must not be null.
	 * @param <T>       the type of the node.
	 * @return the set of entities associated with the node type; if not registered, returns an empty set.
	 */
	public <T extends INode> Set<Entity> getNodeEntities(Class<T> nodeClass) {
		Set<Entity> entities = nodeCollections.get(nodeClass);
		return (entities == null) ? Collections.emptySet() : entities;
		// Note: We could consider returning an unmodifiable set
	}

	/**
	 * Processes an entity to update its memberships across all registered node types.
	 *
	 * @param entity the entity to process; must not be null.
	 */
	public void processEntity(Entity entity) {
		Objects.requireNonNull(entity, "Entity cannot be null");
		// Update membership for each node type based on component requirements.
		nodeRequirements.forEach((nodeType, requirements) -> {
			updateEntityNodeMembership(entity, nodeType, requirements);
		});
	}

	/**
	 * Updates the membership of an entity in a specific node type based on its required components.
	 * <p>
	 * If the entity meets all the required component(s), it is added to the node's collection
	 * and its membership mapping is updated. If the entity no longer meets the requirements,
	 * it is removed from both the node's collection and the membership mapping.
	 *
	 * @param entity       the entity to update; must not be null.
	 * @param nodeClass    the node type to update membership for; must not be null.
	 * @param requirements the set of component classes required by the node type; must not be null.
	 */
	public void updateEntityNodeMembership(Entity entity, Class<? extends INode> nodeClass, Set<Class<? extends IComponent>> requirements) {
		boolean matches = true;
		for (Class<? extends IComponent> componentClass : requirements) {
			if (!entity.hasComponent(componentClass)) {
				matches = false;
				break;
			}
		}

		Set<Entity> nodeEntities = getNodeEntities(nodeClass);
		boolean isInCollection = nodeEntities.contains(entity);

		if (matches && !isInCollection) {
			// Entity meets requirements and is not in the collection; add it
			nodeEntities.add(entity);
			entityNodes.computeIfAbsent(entity, k -> ConcurrentHashMap.newKeySet()).add(nodeClass);
		} else if (!matches && isInCollection) {
			// Entity does not meet requirements but is in the collection; remove it
			nodeEntities.remove(entity);
			Set<Class<? extends INode>> entityNodeTypes = entityNodes.get(entity);
			if (entityNodeTypes != null) {
				entityNodeTypes.remove(nodeClass);
				if (entityNodeTypes.isEmpty()) {
					entityNodes.remove(entity);
				}
			}
		}
	}

	/**
	 * Processes an entity when a component is removed.
	 * <p>
	 * For each node type whose requirements include the removed component, the entity's membership
	 * is updated accordingly.
	 *
	 * @param entity         the entity that had a component removed; must not be null.
	 * @param componentClass the class of the removed component; must not be null.
	 * @param <T>            the type of the component.
	 */
	public <T extends IComponent> void onComponentRemoved(Entity entity, Class<T> componentClass) {
		nodeRequirements.forEach((nodeType, requirements) -> {
			if (requirements.contains(componentClass)) {
				updateEntityNodeMembership(entity, nodeType, requirements);
			}
		});
	}

	/**
	 * Processes all entities in the specified scene to update their node memberships.
	 * <p>
	 * Iterates through each entity in the scene, updating memberships to ensure that each entity's
	 * node associations its current set of components.
	 *
	 * @param scene the scene containing the entities to process; must not be null.
	 */
	public void processScene(Scene scene) {
		scene.getEntities().forEach(this::processEntity);
	}

	/**
	 * Clears all node collections and entity membership mappings.
	 * <p>
	 * Removes all entities from each node's collection and clears the mapping that tracks
	 * which nodes an entity belongs to. Basically resets the NodeManager's state.
	 */
	public void clear() {
		nodeCollections.values().forEach(Set::clear); // Clear each set of entities for each node type
		entityNodes.clear(); // Clear mapping of entities to node memberships
	}
}
