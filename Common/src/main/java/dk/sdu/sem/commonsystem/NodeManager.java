package dk.sdu.sem.commonsystem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NodeManager {
	// Map of node type to collections of entities that match the node
	private final Map<Class<? extends Node>, Set<Node>> nodeCollections = new ConcurrentHashMap<>();

	// Map of entities to the node types (memberships) they belong to
	private final Map<Entity, Set<Node>> entityNodes = new ConcurrentHashMap<>();

	// Cache of required components for each node type
	private final Map<Class<? extends Node>, Set<Class<? extends IComponent>>> nodeRequirements = new ConcurrentHashMap<>();

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
		ServiceLoader.load(Node.class).forEach(n -> {
			nodeRequirements.put(n.getClass(), n.getRequiredComponents());
			nodeCollections.computeIfAbsent(n.getClass(), c -> new HashSet<>());
		});
	}

	/**
	 * Gets all entities that have nodes of the specified type.
	 * @param nodeClass The node class to get entities for
	 * @return Set of entities with nodes of the given type
	 */
	public <T extends Node> Set<Entity> getNodeEntities(Class<T> nodeClass) {
		Set<Node> nodes = nodeCollections.get(nodeClass);
		if (nodes == null) {
			return Collections.emptySet();
		}

		Set<Entity> entities = new HashSet<>();
		for (Node node : nodes) {
			entities.add(node.getEntity());
		}
		return entities;
	}

	@SuppressWarnings("unchecked")
	public <T extends Node> Set<T> getNodes(Class<T> nodeClass) {
		return (Set<T>) nodeCollections.getOrDefault(nodeClass, Collections.emptySet());
	}

	public void processEntity(Entity entity) {
		Set<Node> entityNodeSet = entityNodes.computeIfAbsent(entity, e -> new HashSet<>());

		for (Map.Entry<Class<? extends Node>, Set<Class<? extends IComponent>>> entry : nodeRequirements.entrySet()) {
			Class<? extends Node> nodeType = entry.getKey();
			Set<Class<? extends IComponent>> requirements = entry.getValue();

			// Skip if entity already has this node type
			if (entityNodeSet.stream().anyMatch(n -> n.getClass().equals(nodeType))) {
				continue;
			}

			// Check if entity has all required components
			boolean hasAllComponents = true;
			for (Class<? extends IComponent> component : requirements) {
				if (!entity.hasComponent(component)) {
					hasAllComponents = false;
					break;
				}
			}

			// Create and register node
			if (hasAllComponents) {
				Node node = nodeFactory.createNode(nodeType, entity);
				nodeCollections.get(nodeType).add(node);
				entityNodeSet.add(node);
			}
		}
	}

	public void removeEntity(Entity entity) {
		// I added a little check to prevent NullException if entity doesn't have any nodes (not in entityNodes map)
		Set<Node> nodes = entityNodes.get(entity);
		if (nodes != null) {
			nodes.forEach(node -> {
				nodeCollections.get(node.getClass()).remove(node);
			});
			entityNodes.remove(entity);
		}
	}

	/**
	 * Processes an entity when a component is removed.
	 * <p>
	 * Fo  r each node type whose requirements include the removed component, the entity's membership
	 * is updated accordingly.
	 *
	 * @param entity         the entity that had a component removed; must not be null.
	 * @param componentClass the class of the removed component; must not be null.
	 * @param <T>            the type of the component.
	 */
	public <T extends IComponent> void onComponentRemoved(Entity entity, Class<T> componentClass) {
		Objects.requireNonNull(entity, "Entity cannot be null");
		Objects.requireNonNull(componentClass, "Component class cannot be null");

		Set<Node> entityNodeSet = entityNodes.get(entity);
		if (entityNodeSet == null) {
			return; // No nodes for this entity
		}

		// Collect nodes to remove
		Set<Node> nodesToRemove = new HashSet<>();
		for (Node node : entityNodeSet) {
			for (Class<? extends IComponent> component : nodeRequirements.get(node.getClass())) {
				if (!entity.hasComponent(component)) {
					nodesToRemove.add(node);
					break; // No need to check other components
				}
			}
		}

		// Remove collected nodes
		for (Node node : nodesToRemove) {
			entityNodeSet.remove(node);
			nodeCollections.get(node.getClass()).remove(node);
		}
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
	 * Registers a node type for testing purposes.
	 * This method should only be used in tests.
	 */
	public void registerNodeType(Class<? extends Node> nodeClass, Set<Class<? extends IComponent>> requiredComponents) {
		nodeRequirements.put(nodeClass, requiredComponents);
		nodeCollections.computeIfAbsent(nodeClass, c -> new HashSet<>());
	}

	/**
	 * Creates a node for an entity without adding it to collections.
	 * Useful for testing.
	 * @param nodeClass The node class to create
	 * @param entity The entity to create a node for
	 * @return The created node, or null if the entity doesn't meet requirements
	 */
	public <T extends Node> T createNodeForEntity(Class<T> nodeClass, Entity entity) {
		// Check if entity has all required components
		Set<Class<? extends IComponent>> requirements = nodeRequirements.get(nodeClass);
		if (requirements == null) {
			return null;
		}

		for (Class<? extends IComponent> component : requirements) {
			if (!entity.hasComponent(component)) {
				return null;
			}
		}

		return nodeFactory.createNode(nodeClass, entity);
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