package dk.sdu.sem.gamesystem.factories;

import dk.sdu.sem.commonsystem.Entity;

/**
 * Interface for entity factories.
 * Factories implementing this interface are responsible for creating specific entities.
 */
public interface IEntityFactory {
	/**
	 * Creates an entity with all necessary components.
	 * @return The created entity
	 */
	Entity create();
}