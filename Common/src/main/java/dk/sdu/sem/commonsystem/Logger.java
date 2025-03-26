package dk.sdu.sem.commonsystem;

public class Logger {
	public enum Level {
		ERROR, WARN, INFO, DEBUG, TRACE
	}

	private static Level currentLevel = Level.WARN;

	public static void setLevel(Level level) {
		currentLevel = level;
	}

	public static void error(String message) {
		System.err.println("[ERROR] " + message);
	}

	public static void warn(String message) {
		if (currentLevel.ordinal() >= Level.WARN.ordinal()) {
			System.out.println("[WARN] " + message);
		}
	}

	public static void info(String message) {
		if (currentLevel.ordinal() >= Level.INFO.ordinal()) {
			System.out.println("[INFO] " + message);
		}
	}

	public static void debug(String message) {
		if (currentLevel.ordinal() >= Level.DEBUG.ordinal()) {
			System.out.println("[DEBUG] " + message);
		}
	}

	public static void trace(String message) {
		if (currentLevel.ordinal() >= Level.TRACE.ordinal()) {
			System.out.println("[TRACE] " + message);
		}
	}
}
