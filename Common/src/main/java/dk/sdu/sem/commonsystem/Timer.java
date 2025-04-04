package dk.sdu.sem.commonsystem;

public class Timer {
	private float timer;
	private float duration;

	public Timer(float duration) {
		this.duration = duration;
		this.timer = 0;
	}

	/// @param dt Delta time in seconds
	/// @return True if the timer has elapsed, false otherwise
	public boolean update(float dt) {
		if (dt < 0) { return false; }

		timer += dt;
		if (timer >= duration) {
			timer = 0;
			return true;
		}

		return false;
	}
}
