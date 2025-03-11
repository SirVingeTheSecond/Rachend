package dk.sdu.sem.gamesystem.services;

import dk.sdu.sem.commonsystem.IEntity;

/**
 * interface implemented by listeners
 */
public interface IEntityProcessor {
	// In future make another trigger for more specific
	void process(IEntity entity);
}

