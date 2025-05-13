package dk.sdu.sem.gamesystem;

/**
 * Represents an action scheduled to run after a specific duration.
 */
class ScheduledAction {
	public float duration;
	public float elapsed;
	public Runnable action;

	/**
	 * Creates a new scheduled action.
	 *
	 * @param duration The duration in seconds after which the action should run
	 * @param action The action to run
	 */
	public ScheduledAction(float duration, Runnable action) {
		this.duration = duration;
		this.elapsed = 0;
		this.action = action;
	}
}