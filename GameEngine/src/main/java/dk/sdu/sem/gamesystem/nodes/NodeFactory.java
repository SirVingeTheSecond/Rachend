package dk.sdu.sem.gamesystem.nodes;

import dk.sdu.sem.gamesystem.data.Entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of INodeFactory that creates node instances using reflection.
 */
public class NodeFactory implements INodeFactory {
	// Cache of node instances
	private final Map<Class<? extends Node>, Node> nodeInstanceCache = new ConcurrentHashMap<>();

	@Override
	public <T extends Node> T createNode(Class<T> nodeClass, Entity entity) {
		try {
			T node = nodeClass.getDeclaredConstructor().newInstance();
			node.initialize(entity);
			return node;
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to create node of type " + nodeClass.getName(), e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Node> T getOrCreateNode(Class<T> nodeClass, Entity entity) {
		return (T) nodeInstanceCache.computeIfAbsent(nodeClass, cls -> {
			try {
				return createNode(nodeClass, entity);
			} catch (Exception e) {
				throw new IllegalArgumentException("Failed to create node of type " + nodeClass.getName(), e);
			}
		});
	}

	@Override
	public void clearCache() {
		nodeInstanceCache.clear();
	}
}