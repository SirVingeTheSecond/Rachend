package dk.sdu.sem.gamesystem;

public final class Time {
	// Total elapsed simulation time (in seconds).
	private static double time = 0.0;

	// Delta time (time elapsed since last frame) (in seconds).
	private static double deltaTime = 0.0;

	// Fixed delta time for fixed updates (in seconds). Default for 60Hz.
	private static double fixedDeltaTime = 0.016667;

	// Global time scale (1.0 = normal speed).
	private static double timeScale = 1.0;

	private Time() {
		// Prevent instantiation.
	}

	public static double getTime() {
		return time;
	}

	public static double getDeltaTime() {
		return deltaTime;
	}

	public static double getFixedDeltaTime() {
		return fixedDeltaTime;
	}

	public static double getTimeScale() {
		return timeScale;
	}

	public static void setTimeScale(double newTimeScale) {
		if (newTimeScale >= 0) {
			timeScale = newTimeScale;
		}
	}

	/**
	 * Call this during each variable-rate update (from the UI thread).
	 * @param dt Time in seconds since the last frame.
	 */
	public static void update(double dt) {
		deltaTime = dt * timeScale;
		time += deltaTime;
	}
}
