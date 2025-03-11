package dk.sdu.sem.gamesystem.services;

import dk.sdu.sem.commonsystem.IEntity;
import dk.sdu.sem.gamesystem.data.Entity;

/**
 * interface implemented by listeners
 */
public interface IEntityPostProcessor {
	// In future make another trigger for more specific
	void postProcess(IEntity entity);
}

