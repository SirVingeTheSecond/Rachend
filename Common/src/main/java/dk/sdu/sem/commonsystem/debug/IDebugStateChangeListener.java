package dk.sdu.sem.commonsystem.debug;

/**
 * Interface for listeners that need to be notified when debug state changes.
 */
public interface IDebugStateChangeListener {
	/**
	 * Called when debug state changes
	 */
	void onDebugStateChanged();
}