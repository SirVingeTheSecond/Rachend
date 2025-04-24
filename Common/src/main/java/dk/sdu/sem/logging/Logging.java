package dk.sdu.sem.logging;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Logging {
	private static Map<String, Logging> loggers = new HashMap<>();
	public static FilterMode filterMode = new FilterMode.All();

	private static PrintStream out = System.out;

	private LoggingLevel level;
	private String name;

	private Logging(LoggingLevel level, String name) {
		this.level = level;
		this.name = name;
	}

	public static void solo(String name) {
		Logging.filterMode = new FilterMode.Include(List.of(name));
	}

	public static void only(List<String> names) {
		Logging.filterMode = new FilterMode.Include(names);
	}
	public static void all() { Logging.filterMode = new FilterMode.All(); }
	public static void none() { Logging.filterMode = new FilterMode.None(); }
	public static void not(List<String> names) { Logging.filterMode = new FilterMode.Exclude(names); }

	public static Logging createLogger(String name, LoggingLevel level) {
		return loggers.computeIfAbsent(name, (__) -> new Logging(level, name));
	}


	public void debug(String message, Object... args) { log(LoggingLevel.DEBUG, message, out::println, args); }
	public void info(String message, Object... args) { log(LoggingLevel.INFO, message, out::println, args); }
	public void warn(String message, Object... args) { log(LoggingLevel.WARN, message, out::println, args); }
	public void error(String message, Object... args) { log(LoggingLevel.ERROR, message, out::println, args); }

	private void log(LoggingLevel level, String message, Consumer<String> out, Object... args) {
		if (filterMode.accept(name) == false) { return; }
		if (level.value < this.level.value) { return; }
		out.accept(String.format("[ %s %s ] %s", level, name, String.format(message, args)));
	}

	public void print(LoggingLevel level, String message) {	log(level, message, out::print); }
	public void println(LoggingLevel level, String message) { log(level, message, out::println); }
}
