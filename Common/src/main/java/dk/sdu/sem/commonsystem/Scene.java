package dk.sdu.sem.commonsystem;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Scene {
	private final String name;
	private final Set<Entity> entities = new HashSet<>();
	private final Set<Entity> persistedEntities = new HashSet<>();
	private final NodeManager nodeManager;

	private static Scene activeScene;

	public Scene(String name) {
		this.name = name;
		this.nodeManager = new NodeManager(new NodeFactory());
	}

	public static Scene getActiveScene() {
		return activeScene;
	}

	public static void setActiveScene(Scene activeScene) {
		Scene.activeScene = activeScene;
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
	 * Adds an entity to the scene, entities already present in the scene will be ignored
	 * @param entity The entity to add
	 */
	public void addEntity(Entity entity) {
		Objects.requireNonNull(entity, "Entity cannot be null");

		// Entity is already in the scene?
		if (entities.contains(entity)) {
			return;
		}

		entities.add(entity);

		entity.setScene(this);

		nodeManager.processEntity(entity);

		System.out.printf("Added entity %s to scene %s \n", entity.getID(), getName());
	}

	/**
	 * Removes an entity from the scene if it is present. Will also remove
	 * from list of persisted entities.
	 * @param entity The entity to remove
	 */
	public void removeEntity(Entity entity) {
		persistedEntities.remove(entity);
		if (entities.remove(entity)) {
			// Clean up node references in the NodeManager
			nodeManager.removeEntity(entity);

			// Also clean node factory cache
			if (nodeManager.getNodeFactory() instanceof NodeFactory) {
				((NodeFactory) nodeManager.getNodeFactory()).removeEntityFromCache(entity);
			}

			// Set scene to null to help with garbage collection
			entity.setScene(null);

			System.out.printf("Removed entity %s from scene %s \n", entity.getID(), getName());
		}
	}

	/**
	 * Called when a component is added to an entity in this scene
	 */
	public void onComponentAdded(Entity entity, Class<? extends IComponent> componentClass) {
		nodeManager.onComponentAdded(entity, componentClass);
	}

	/**
	 * Called when a component is removed from an entity in this scene
	 */
	public <T extends IComponent> void onComponentRemoved(Entity entity, Class<T> componentClass) {
		nodeManager.onComponentRemoved(entity, componentClass);

		// If the entity is in NodeFactory cache, invalidate the affected nodes
		if (nodeManager.getNodeFactory() instanceof NodeFactory) {
			NodeFactory factory = (NodeFactory) nodeManager.getNodeFactory();

			// We need to invalidate cached nodes that require this component
			for (Class<? extends Node> nodeType : nodeManager.getNodeTypes()) {
				Set<Class<? extends IComponent>> requirements = nodeManager.getNodeRequirements(nodeType);
				if (requirements.contains(componentClass)) {
					factory.invalidateNode(nodeType, entity);
				}
			}
		}
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

	/**
	 * Clears all entities from the scene.
	 */
	public void clear() {
		// Make a copy to avoid concurrency issues
		Set<Entity> entitiesToRemove = new HashSet<>(entities);
		for (Entity entity : entitiesToRemove) {
			removeEntity(entity);
		}

		persistedEntities.clear();
		nodeManager.clear();
	}
}