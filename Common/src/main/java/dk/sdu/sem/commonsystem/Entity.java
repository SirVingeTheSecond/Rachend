package dk.sdu.sem.commonsystem;

import java.util.*;

public class Entity {
	private final UUID ID = UUID.randomUUID();
	private final Map<Class<?>, IComponent> components = new HashMap<>();
	private Scene scene; // Reference to the scene this entity belongs to

	public Entity() {

	}

	/**
	 * Sets the scene this entity belongs to
	 * This is set automatically when adding the entity to a scene
	 */
	public void setScene(Scene scene) {
		this.scene = scene;
	}

	/**
	 * Gets the scene this entity belongs to
	 */
	public Scene getScene() {
		return scene;
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
	public <T extends IComponent> T getComponent(Class<T> componentClass) {
		// First we do direct lookup
		IComponent component = components.get(componentClass);
		if (component != null) {
			return (T) component;
		}

		// Then check for subclass components
		for (Map.Entry<Class<?>, IComponent> entry : components.entrySet()) {
			if (componentClass.isAssignableFrom(entry.getKey())) {
				return (T) entry.getValue();
			}
		}
		return null;
	}

	/**
	 * Get all components as a set.
	 * @return A set of all components.
	 */
	public Set<IComponent> getAllComponents() {
		return new HashSet<>(components.values());
	}

	/**
	 * Add a component to this entity.
	 * @param component The component to add
	 * @param <T> Type of component extending Component interface
	 */
	public <T extends IComponent> void addComponent(IComponent component){
		components.put(component.getClass(), component);

		// Notify scene of component addition if entity is in a scene
		if (scene != null) {
			scene.onComponentAdded(this, component.getClass());
		}
	}

	/**
	 * Remove a component by type.
	 * @param componentClass The class of the component to remove
	 */
	public <T extends IComponent> void removeComponent(Class<T> componentClass){
		IComponent removed = components.remove(componentClass);

		// Notify scene of component removal if entity is in a scene and component was removed
		if (scene != null && removed != null) {
			scene.onComponentRemoved(this, componentClass);
		}
	}

	/**
	 * Check if this entity has a component of the specified type.
	 * @param componentClass The class of the component to check
	 * @return true if the entity has the component, false otherwise
	 */
	public <T extends IComponent> boolean hasComponent(Class<T> componentClass) {
		// Direct check first
		if (components.containsKey(componentClass)) {
			return true;
		}

		// Then check for subclass components
		for (Class<?> cls : components.keySet()) {
			if (componentClass.isAssignableFrom(cls)) {
				return true;
			}
		}
		return false;
	}
}