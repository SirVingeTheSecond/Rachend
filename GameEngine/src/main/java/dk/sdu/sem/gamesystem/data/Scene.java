package dk.sdu.sem.gamesystem.data;

import dk.sdu.sem.gamesystem.components.IComponent;
import dk.sdu.sem.gamesystem.nodes.INode;
import dk.sdu.sem.gamesystem.nodes.NodeFactory;
import dk.sdu.sem.gamesystem.nodes.NodeManager;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

public class Scene {
	private String name;
	private final Set<Entity> entities = new HashSet<>();
	private final Set<Entity> persistedEntities = new HashSet<>();
	private final NodeManager nodeManager;

	public Scene(String name) {
		this.name = name;
		this.nodeManager = new NodeManager(new NodeFactory());

		// Register all node types available through ServiceLoader
		ServiceLoader.load(INode.class).forEach(node -> {
			Class<? extends INode> nodeClass = node.getClass();
			nodeManager.registerNodeType(nodeClass);
		});
	}

	/**
	 * @return Set of all entities in the scene
	 */
	public Set<Entity> getEntities() {
		return entities;
	}

	/**
	 * @return Set of entities in the scene that contain the specified component
	 */
	public Set<Entity> getEntitiesWithComponent(Class<? extends IComponent> component) {
		return entities.stream()
				.filter(e -> e.hasComponent(component))
				.collect(Collectors.toSet());
	}

	/**
	 * @return Set of entities that match the specified node type
	 */
	public <T extends INode> Set<Entity> getEntitiesForNode(Class<T> nodeClass) {
		return nodeManager.getNodeEntities(nodeClass);
	}

	/**
	 * Adds an entity to the scene, entities already present in the scene will be ignored
	 * @param entity The entity to add
	 */
	public void addEntity(Entity entity) {
		if (entities.add(entity)) {
			// If the entity was added (wasn't already in the scene)
			entity.setScene(this);
			nodeManager.processEntity(entity);
		}
	}

	/**
	 * Removes an entity from the scene if it is present. Will also remove
	 * from list of persisted entities.
	 * @param entity The entity to remove
	 */
	public void removeEntity(Entity entity) {
		persistedEntities.remove(entity);
		if (entities.remove(entity)) {
			entity.setScene(null);
		}
	}

	/**
	 * Called when a component is added to an entity in this scene
	 */
	public void onComponentAdded(Entity entity, Class<? extends IComponent> componentClass) {
		nodeManager.processEntity(entity);
	}

	/**
	 * Called when a component is removed from an entity in this scene
	 */
	public <T extends IComponent> void onComponentRemoved(Entity entity, Class<T> componentClass) {
		nodeManager.onComponentRemoved(entity, componentClass);
	}

	/**
	 * Gets the NodeManager for this scene
	 */
	public NodeManager getNodeManager() {
		return nodeManager;
	}

	public void addPersistedEntity(Entity entity) {
		persistedEntities.add(entity);
	}

	public void removePersistedEntity(Entity entity) {
		persistedEntities.remove(entity);
	}

	public Set<Entity> getPersistedEntities() {
		return persistedEntities;
	}

	public String getName() {
		return name;
	}
}