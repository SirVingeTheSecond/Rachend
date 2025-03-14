package dk.sdu.sem.gamesystem.nodes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of INodeFactory that creates node instances using reflection.
 */
public class NodeFactory implements INodeFactory {
	// Cache of node instances
	private final Map<Class<? extends INode>, INode> nodeInstanceCache = new ConcurrentHashMap<>();

	@Override
	public <T extends INode> T createNode(Class<T> nodeClass) {
		try {
			return nodeClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to create node of type " + nodeClass.getName(), e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends INode> T getOrCreateNode(Class<T> nodeClass) {
		return (T) nodeInstanceCache.computeIfAbsent(nodeClass, cls -> {
			try {
				return createNode(nodeClass);
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