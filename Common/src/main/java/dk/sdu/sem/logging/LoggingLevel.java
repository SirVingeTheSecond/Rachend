package dk.sdu.sem.logging;

public enum LoggingLevel {
	NONE(0),
	DEBUG(1),
	INFO(2),
	WARN(3),
	ERROR(4),
	FATAL(5),
	ALL(100);

	final int value;

	LoggingLevel(int value) {
		this.value = value;
	}
}
