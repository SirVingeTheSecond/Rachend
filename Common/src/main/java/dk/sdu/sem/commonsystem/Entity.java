package dk.sdu.sem.commonsystem;

import java.util.*;
import java.util.function.Supplier;

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
	 * @return IComponent The component added to this entity
	 */
	public <T extends IComponent> T addComponent(T component){
		components.put(component.getClass(), component);

		// Notify scene of component addition if entity is in a scene
		if (scene != null) {
			scene.onComponentAdded(this, component.getClass());
		}
		return component;
	}

	/**
	 * Remove a component by type.
	 * @param componentClass The class of the component to remove
	 */
	public <T extends IComponent> void removeComponent(Class<T> componentClass){
		if (scene == null)
			return;

		IComponent removed = components.remove(componentClass);

		// Notify scene of component removal if entity is in a scene and component was removed
		if (removed != null) {
			scene.onComponentRemoved(this, componentClass);
		}

		// Then check for subclass components
		// Use iterator to not cause cuncurrent modification exception
		Iterator<Map.Entry<Class<?>, IComponent>> iterator = components.entrySet().iterator();
		iterator.forEachRemaining(entry -> {
			if (componentClass.isAssignableFrom(entry.getKey())) {
				scene.onComponentRemoved(this, entry.getValue().getClass());
				iterator.remove();
			}
		});
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

	/// Ensure that this entity has a component
	/// @param type The component type that must be on this entity
	/// @param supplier The supplier lambda that returns a new instance of the component if this entity does not already have it
	/// @return IComponent The component that is ensured this entity has
    public <T extends IComponent> T ensure(Class<T> type, Supplier<T> supplier) {
		if (hasComponent(type)) { return getComponent(type); }
		return (T)addComponent(supplier.get());
	}
}