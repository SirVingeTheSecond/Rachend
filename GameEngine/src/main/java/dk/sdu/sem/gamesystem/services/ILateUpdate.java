package dk.sdu.sem.gamesystem.services;

import dk.sdu.sem.commonsystem.IEntity;

/**
 * interface implemented by listeners
 */
public interface ILateUpdate {
	// In future make another trigger for more specific
	void lateUpdate();
}

