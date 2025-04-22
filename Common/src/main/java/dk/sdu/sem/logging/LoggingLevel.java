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
}
