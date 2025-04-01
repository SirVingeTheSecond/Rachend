package dk.sdu.sem.gamesystem;

import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.commonitem.IItemFactory;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.enemy.IEnemyFactory;
import dk.sdu.sem.gamesystem.factories.IEntityFactory;
import dk.sdu.sem.player.IPlayerFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Utility class for locating service implementations via ServiceLoader.
 */
public class ServiceLocator {

	/**
	 * Gets the collision system if available.
	 *
	 * @return The first available {@link ICollisionSPI} or null if the collision module is not present.
	 */
	public static ICollisionSPI getCollisionSystem() {
		Iterator<ICollisionSPI> services = ServiceLoader.load(ICollisionSPI.class).iterator();
		return services.hasNext() ? services.next() : null;
	}

	/**
	 * Gets all entity factory implementations.
	 *
	 * @return An iterator over all {@link IEntityFactory} implementations.
	 */
	public static Iterator<? extends IEntityFactory> getEntityFactories() {
		return ServiceLoader.load(IEntityFactory.class).iterator();
	}

	/**
	 * Gets all player factory implementations.
	 *
	 * @return An iterator over all {@link IPlayerFactory} implementations.
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
	 * Gets the first entity factory of a specific type.
	 *
	 * @param factoryType The class type of the desired entity factory.
	 * @param <T>         The type parameter extending {@link IEntityFactory}.
	 * @return The first matching entity factory, or null if none is found.
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
	 *
	 * @return The first available {@link IPlayerFactory} or null if none is found.
	 */
	public static IPlayerFactory getPlayerFactory() {
		Iterator<? extends IPlayerFactory> factories = getPlayerFactories();
		return factories.hasNext() ? factories.next() : null;
	}

	/**
	 * Gets the collider factory if available.
	 *
	 * @return The first available {@link IColliderFactory} or null if the collision module is not present.
	 */
	public static IColliderFactory getColliderFactory() {
		Iterator<IColliderFactory> factories = ServiceLoader.load(IColliderFactory.class).iterator();
		return factories.hasNext() ? factories.next() : null;
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

	public static IItemFactory getItemFactory() {
		Iterator<IItemFactory> factories = ServiceLoader.load(IItemFactory.class).iterator();
		return factories.hasNext() ? factories.next() : null;
	}
}