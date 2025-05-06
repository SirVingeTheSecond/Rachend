package dk.sdu.sem.gamesystem;

public class ScheduledAction {
	public float duration;
	public Runnable action;
	public float elapsed = 0;

	public ScheduledAction(float duration, Runnable action) {
		this.duration = duration;
		this.action = action;
	}
}