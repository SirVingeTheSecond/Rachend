package dk.sdu.sem.gamesystem.data;

import dk.sdu.sem.gamesystem.SceneManager;
import dk.sdu.sem.gamesystem.components.IComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Entity {
	private final UUID ID = UUID.randomUUID();
	private final Map<Class<?>, IComponent> components = new HashMap<>();

	public Entity() {

	}

	public String getID() {
		return ID.toString();
	}

	/**
	 * Get a component by type.
	 * @param componentClass The class of the component to get
	 * @return The component if present, null otherwise
	 */
	@SuppressWarnings("unchecked")
	public <T extends IComponent> T getComponent(Class<T> componentClass){
		return (T) components.get(componentClass);
	}

	/**
	 * Add a component to this entity.
	 * @param component The component to add
	 * @param <T> Type of component extending Component interface
	 */
	public <T extends IComponent> void addComponent(Class<T> componentClass, IComponent component){
		components.put(componentClass, component);
	}

	/**
	 * Remove a component by type.
	 * @param componentClass The class of the component to remove
	 */
	public <T extends IComponent> void removeComponent(Class<T> componentClass, IComponent component){
		components.remove(componentClass, component);
	}

	/**
	 * Check if this entity has a component of the specified type.
	 * @param componentClass The class of the component to check
	 * @return true if the entity has the component, false otherwise
	 */
	public <T extends IComponent> boolean hasComponent(Class<T> componentClass){
		return components.containsKey(componentClass);
	}

	/**
	 * Persists the entity between scene changes
	 */
	public void persist() {
		SceneManager.getInstance().addPersistedEntity(this);
	}

	/**
	 * Stops the entity from persisting between scene changes
	 * (Default behaviour)
	 */
	public void unPersist() {
		SceneManager.getInstance().removePersistedEntity(this);
	}
}