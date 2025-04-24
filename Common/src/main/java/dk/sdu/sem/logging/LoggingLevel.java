package dk.sdu.sem.logging;

public enum LoggingLevel {
	ALL(0),
	DEBUG(10),
	INFO(20),
	WARN(40),
	ERROR(60),
	FATAL(80),
	NONE(100);

	final int value;

	LoggingLevel(int value) {
		this.value = value;
	}

	public String ansi() {
		return switch (this) {
			case INFO -> "\033[34m"; // blue
			case ERROR -> "\033[31m"; // red
			case WARN -> "\033[33m"; // yellow
			case FATAL -> "\033[101m";
			default -> "";
		};
	}
}
