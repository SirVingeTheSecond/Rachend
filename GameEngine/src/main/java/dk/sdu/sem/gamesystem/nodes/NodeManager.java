package dk.sdu.sem.gamesystem.nodes;

import dk.sdu.sem.gamesystem.components.IComponent;
import dk.sdu.sem.gamesystem.data.Entity;
import dk.sdu.sem.gamesystem.data.Scene;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NodeManager {
	// Map of node type to collections of entities that match the node
	private final Map<Class<? extends INode>, Set<INode>> nodeCollections = new ConcurrentHashMap<>();

	// Map of entities to the node types (memberships) they belong to
	private final Map<Entity, Set<INode>> entityNodes = new ConcurrentHashMap<>();

	// Cache of required components for each node type
	private final Map<Class<? extends INode>, Set<Class<? extends IComponent>>> nodeRequirements = new ConcurrentHashMap<>();

	private final INodeFactory nodeFactory;

	/**
	 * Constructs a new NodeManager with the specified NodeFactory.
	 *
	 * @param nodeFactory the factory used to obtain or create node instances; must not be null.
	 */
	public NodeManager(INodeFactory nodeFactory) {
		this.nodeFactory = Objects.requireNonNull(nodeFactory, "NodeFactory cannot be null");
		getNodeRequirements();
	}

	private void getNodeRequirements() {
		ServiceLoader.load(INode.class).forEach(n -> {
			nodeRequirements.put(n.getClass(), n.getRequiredComponents());
			nodeCollections.computeIfAbsent(n.getClass(), c -> new HashSet<>());
		});
	}

	@SuppressWarnings("unchecked")
	public <T extends INode> Set<T> getNodes(Class<T> nodeClass) {
		return (Set<T>) nodeCollections.get(nodeClass);
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
	@Deprecated
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
	@Deprecated
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
	/*
	public <T extends INode> Set<Entity> getNodeEntities(Class<T> nodeClass) {
		Set<Entity> entities = nodeCollections.get(nodeClass);
		return (entities == null) ? Collections.emptySet() : Collections.unmodifiableSet(entities);
	}*/

	/**
	 * Processes an entity to update its memberships across all registered node types.
	 *
	 * @param entity the entity to process; must not be null.
	 */
	/*@Deprecated
	public void processEntity(Entity entity) {

		Objects.requireNonNull(entity, "Entity cannot be null");
		// Update membership for each node type based on component requirements
		nodeRequirements.forEach((nodeType, requirements) -> {
			updateEntityNodeMembership(entity, nodeType, requirements);
		});
	}*/

	public void processEntity(Entity entity) {
		nodeRequirements.forEach((nodeType, requirements) -> {
			if (entityNodes.get(entity).stream().anyMatch(n -> n.getClass() == nodeType)) {
				return;
			}

			boolean createNode = true;
			for (Class<? extends IComponent> component : requirements) {
				if (!entity.hasComponent(component)) {
					createNode = false;
					break;
				}
			}

			if (createNode) {
				INode node = nodeFactory.createNode(nodeType);
				nodeCollections.get(nodeType).add(node);
				entityNodes.get(entity).add(node);
			}
		});
	}

	public void removeEntity(Entity entity) {
		entityNodes.get(entity).forEach(node -> {
			nodeCollections.get(node.getClass()).remove(node);
		});
		entityNodes.remove(entity);
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
	/*
	public void updateEntityNodeMembership(Entity entity, Class<? extends INode> nodeClass, Set<Class<? extends IComponent>> requirements) {
		boolean matches = true;
		for (Class<? extends IComponent> componentClass : requirements) {
			if (!entity.hasComponent(componentClass)) {
				matches = false;
				break;
			}
		}

		Set<Entity> nodeEntities = nodeCollections.get(nodeClass);
		if (nodeEntities == null) {
			// If the node type is not registered, do nothing
			return;
		}

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
	}*/

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
		Objects.requireNonNull(entity, "Entity cannot be null");
		Objects.requireNonNull(componentClass, "Component class cannot be null");

		entityNodes.get(entity).iterator().forEachRemaining(node -> {
			for (Class<? extends IComponent> component : nodeRequirements.get(node.getClass())) {
				if (!entity.hasComponent(component)) {
					entityNodes.get(entity).remove(node);
					nodeCollections.get(node.getClass()).remove(node);
				}
			}
		});
	}

	/**
	 * Processes an entity when a component is added.
	 * <p>
	 * For each node type whose requirements include the removed component, the entity's membership
	 * is updated accordingly.
	 *
	 * @param entity         the entity that had a component removed; must not be null.
	 * @param componentClass the class of the removed component; must not be null.
	 * @param <T>            the type of the component.
	 */
	public <T extends IComponent> void onComponentAdded(Entity entity, Class<T> componentClass) {
		Objects.requireNonNull(entity, "Entity cannot be null");
		Objects.requireNonNull(componentClass, "Component class cannot be null");

		processEntity(entity);
	}

	/**
	 * Gets a node instance for an entity if it matches the node type requirements.
	 *
	 * @param <T> The node type
	 * @param nodeClass The class of the node type
	 * @param entity The entity to create a node for
	 * @return A node instance or null if the entity doesn't match the requirements
	 */
	@Deprecated
	public <T extends INode> T createNodeForEntity(Class<T> nodeClass, Entity entity) {
		Set<Class<? extends IComponent>> requirements = nodeRequirements.get(nodeClass);
		if (requirements == null) {
			registerNodeType(nodeClass);
			requirements = nodeRequirements.get(nodeClass);
		}

		// Check if entity has all required components
		for (Class<? extends IComponent> componentClass : requirements) {
			if (!entity.hasComponent(componentClass)) {
				return null; // Entity doesn't match requirements
			}
		}

		// Create a node instance for this entity
		try {
			// Find constructor that takes an Entity parameter
			java.lang.reflect.Constructor<T> constructor = nodeClass.getConstructor(Entity.class);
			return constructor.newInstance(entity);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to create node for entity", e);
		}
	}

	/**
	 * Processes all entities in the specified scene to update their node memberships.
	 * <p>
	 * Iterates through each entity in the scene, updating memberships to ensure that each entity's
	 * node associations its current set of components.
	 *
	 * @param scene the scene containing the entities to process; must not be null.
	 */
	@Deprecated
	public void processScene(Scene scene) {
		Objects.requireNonNull(scene, "Scene cannot be null");
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