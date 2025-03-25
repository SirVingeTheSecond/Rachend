package dk.sdu.sem.gamesystem.assets;

/**
 * Interface for objects that need to release resources explicitly.
 * Implementing classes should clean up any resources they hold
 * when dispose() is called.
 */
public interface IDisposable {
	/**
	 * Releases all resources held by this object.
	 * After disposal, the object should no longer be used.
	 */
	void dispose();

	/**
	 * Checks if this object has been disposed.
	 * @return true if the object has been disposed, else false
	 */
	boolean isDisposed();
}