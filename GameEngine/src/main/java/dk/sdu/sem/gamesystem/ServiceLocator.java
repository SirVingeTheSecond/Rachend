package dk.sdu.sem.gamesystem;

import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.gamesystem.factories.IEntityFactory;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;
import dk.sdu.sem.gamesystem.services.ILateUpdate;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.player.IPlayerFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Utility class for locating service implementations via ServiceLoader.
 * Provides a consistent way to access all service types in the system.
 */
public class ServiceLocator {
	/*
	public static Iterator<? extends Node> getNodes() {
		return ServiceLoader.load(Node.class).iterator();
	}

	public static Iterator<? extends IFixedUpdate> getFixedUpdates() {
		return ServiceLoader.load(IFixedUpdate.class).iterator();
	}

	public static Iterator<? extends IUpdate> getUpdates() {
		return ServiceLoader.load(IUpdate.class).iterator();
	}

	public static Iterator<? extends ILateUpdate> getLateUpdates() {
		return ServiceLoader.load(ILateUpdate.class).iterator();
	}
	*/

	/**
	 * Gets all entity factory implementations.
	 */
	public static Iterator<? extends IEntityFactory> getEntityFactories() {
		return ServiceLoader.load(IEntityFactory.class).iterator();
	}

	/**
	 * Gets all player factory implementations.
	 */
	public static Iterator<? extends IPlayerFactory> getPlayerFactories() {
		return ServiceLoader.load(IPlayerFactory.class).iterator();
	}

	/**
	 * Gets first entity factory of a specific type.
	 */
	public static <T extends IEntityFactory> T getEntityFactory(Class<T> factoryType) {
		Iterator<? extends IEntityFactory> factories = getEntityFactories();
		while (factories.hasNext()) {
			IEntityFactory factory = factories.next();
			if (factoryType.isInstance(factory)) {
				return factoryType.cast(factory);
			}
		}
		return null;
	}

	/**
	 * Gets the first available player factory.
	 */
	public static IPlayerFactory getPlayerFactory() {
		Iterator<? extends IPlayerFactory> factories = getPlayerFactories();
		if (factories.hasNext()) {
			return factories.next();
		}
		return null;
	}

	/**
	 * Gets the collider factory if available.
	 * @return The collider factory or null if collision module is not present
	 */
	public static IColliderFactory getColliderFactory() {
		ServiceLoader<IColliderFactory> loader = ServiceLoader.load(IColliderFactory.class);
		Iterator<IColliderFactory> factories = loader.iterator();

		if (factories.hasNext()) {
			IColliderFactory factory = factories.next();
			System.out.println("DEBUG: Found collider factory: " + factory.getClass().getName());
			return factory;
		}

		System.out.println("DEBUG: No collider factory found");
		return null;
	}
}