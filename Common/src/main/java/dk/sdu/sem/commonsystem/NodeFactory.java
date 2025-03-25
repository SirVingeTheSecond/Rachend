package dk.sdu.sem.commonsystem;

import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory class for creating node instances via NodeProviders.
 * Utilizes a caching system to ensure unique node instances for each entity.
 * <p>
 * Note: With each node now implementing its own create method,
 * the necessity of this factory could be questioned.
 */
public class NodeFactory implements INodeFactory {
	// Map of node class to its provider
	private final Map<Class<? extends Node>, INodeProvider<?>> providers = new ConcurrentHashMap<>();

	// Node Type -> Entity ID -> Node Instance
	private final Map<Class<? extends Node>, Map<String, Node>> nodeCache = new ConcurrentHashMap<>();

	public NodeFactory() {
		loadNodeProviders();
	}

	/**
	 * Loads all node providers using ServiceLoader.
	 */
	private void loadNodeProviders() {
		ServiceLoader.load(INodeProvider.class).forEach(provider ->
			providers.put(provider.getNodeType(), provider)); // unchecked for now :(
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Node> T createNode(Class<T> nodeClass, Entity entity) {
		Objects.requireNonNull(nodeClass, "Node class cannot be null");
		Objects.requireNonNull(entity, "Entity cannot be null");

		// Get provider for node type
		INodeProvider<?> provider = providers.get(nodeClass);
		if (provider == null) {
			throw new IllegalArgumentException("No provider registered for node type: " + nodeClass.getName());
		}

		// Create and initialize node
		Node node = provider.create();
		node.initialize(entity);
		return (T) node;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Node> T getOrCreateNode(Class<T> nodeClass, Entity entity) {
		Objects.requireNonNull(nodeClass, "Node class cannot be null");
		Objects.requireNonNull(entity, "Entity cannot be null");

		// Get or create the entity cache for this node type
		Map<String, Node> entityCache = nodeCache.computeIfAbsent(
			nodeClass, k -> new ConcurrentHashMap<>()
		);

		// Get or create node for this entity
		return (T) entityCache.computeIfAbsent(
			entity.getID(), id -> createNode(nodeClass, entity)
		);
	}

	/**
	 * Removes a specific entity's nodes from the cache.
	 * Call this when an entity is removed from a scene.
	 */
	public void removeEntityFromCache(Entity entity) {
		if (entity == null) return;

		String entityId = entity.getID();
		// Remove from all node type caches
		for (Map<String, Node> entityCache : nodeCache.values()) {
			entityCache.remove(entityId);
		}
	}

	/**
	 * Removes a specific cached node for an entity.
	 *
	 * Should be called when an entity loses a component that was
	 * required by this node type, making the cached node invalid.
	 *
	 */
	public void invalidateNode(Class<? extends Node> nodeClass, Entity entity) {
		if (nodeClass == null || entity == null) return;

		Map<String, Node> entityCache = nodeCache.get(nodeClass);
		if (entityCache != null) {
			entityCache.remove(entity.getID());
		}
	}

	@Override
	public void clearCache() {
		nodeCache.clear();
	}
}