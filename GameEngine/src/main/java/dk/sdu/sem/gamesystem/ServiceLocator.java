package dk.sdu.sem.gamesystem;

import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.enemy.IEnemyFactory;
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
	 * Gets all enemy factory implementations.
	 */
	public static Iterator<? extends IEnemyFactory> getEnemyFactories() {
		return ServiceLoader.load(IEnemyFactory.class).iterator();
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
	 * Gets the first available enemy factory.
	 */
	public static IEnemyFactory getEnemyFactory() {
		Iterator<? extends IEnemyFactory> factories = getEnemyFactories();
		if(factories.hasNext()) {
			return factories.next();
		}
		return null;
	}
}