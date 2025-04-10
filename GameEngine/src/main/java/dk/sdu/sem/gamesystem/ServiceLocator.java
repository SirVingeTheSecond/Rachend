package dk.sdu.sem.gamesystem;

import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.commonitem.IItemFactory;
import dk.sdu.sem.enemy.IEnemyFactory;
import dk.sdu.sem.gamesystem.factories.IEntityFactory;
import dk.sdu.sem.player.IPlayerFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Utility class for locating service implementations via ServiceLoader.
 * Implements caching to ensure consistent instances throughout the application.
 */
public class ServiceLocator {
	// Cached service instances
	private static ICollisionSPI collisionSystem;
	private static IColliderFactory colliderFactory;
	private static IPlayerFactory playerFactory;
	private static IEnemyFactory enemyFactory;
	private static IItemFactory itemFactory;

	// For testing or scene transitions
	public static void reset() {
		collisionSystem = null;
		colliderFactory = null;
		playerFactory = null;
		enemyFactory = null;
		itemFactory = null;
	}

	/**
	 * Gets the collision system if available.
	 *
	 * @return The first available {@link ICollisionSPI} or null if the collision module is not present.
	 */
	// We should keep a close eye on this for performance reasons
	// Who are calling this and how many times is it called?
	public static ICollisionSPI getCollisionSystem() {
		if (collisionSystem == null) {
			Iterator<ICollisionSPI> services = ServiceLoader.load(ICollisionSPI.class).iterator();
			if (services.hasNext()) {
				collisionSystem = services.next();
				System.out.println("Collision service newly loaded and cached in ServiceLocator");
			}
		}
		//System.out.println("Returning cached collision service from ServiceLocator");
		return collisionSystem;
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
		if (playerFactory == null) {
			Iterator<? extends IPlayerFactory> factories = getPlayerFactories();
			if (factories.hasNext()) {
				playerFactory = factories.next();
			}
		}
		return playerFactory;
	}

	/**
	 * Gets the collider factory if available.
	 *
	 * @return The first available {@link IColliderFactory} or null if the collision module is not present.
	 */
	public static IColliderFactory getColliderFactory() {
		if (colliderFactory == null) {
			Iterator<IColliderFactory> factories = ServiceLoader.load(IColliderFactory.class).iterator();
			if (factories.hasNext()) {
				colliderFactory = factories.next();
			}
		}
		return colliderFactory;
	}

	/**
	 * Gets the first available enemy factory.
	 */
	public static IEnemyFactory getEnemyFactory() {
		if (enemyFactory == null) {
			Iterator<? extends IEnemyFactory> factories = getEnemyFactories();
			if (factories.hasNext()) {
				enemyFactory = factories.next();
			}
		}
		return enemyFactory;
	}

	/**
	 * Gets the first available item factory.
	 */
	public static IItemFactory getItemFactory() {
		if (itemFactory == null) {
			Iterator<IItemFactory> factories = ServiceLoader.load(IItemFactory.class).iterator();
			if (factories.hasNext()) {
				itemFactory = factories.next();
			}
		}
		return itemFactory;
	}
}