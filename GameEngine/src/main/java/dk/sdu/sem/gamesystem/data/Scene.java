package dk.sdu.sem.gamesystem.data;

import dk.sdu.sem.gamesystem.components.IComponent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Scene {
	private String name;

	private Set<Entity> entities = new HashSet<>();

	public Scene(String name) {
		this.name = name;
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
		entities.add(entity);
	}

	/**
	 * Removes an entity from the scene if it is present
	 * @param entity The entity to remove
	 */
	public void removeEntity(Entity entity) {
		entities.remove(entity);
	}

	public String getName() {
		return name;
	}
}
