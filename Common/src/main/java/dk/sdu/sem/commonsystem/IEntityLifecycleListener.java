package dk.sdu.sem.commonsystem;

/**
 * Interface for entity lifecycle events
 */
public interface IEntityLifecycleListener {
	/**
	 * Called when an entity is removed from a scene
	 */
	void onEntityRemoved(Entity entity);
}
