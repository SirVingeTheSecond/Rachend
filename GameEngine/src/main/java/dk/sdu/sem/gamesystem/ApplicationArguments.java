package dk.sdu.sem.gamesystem;

import dk.sdu.sem.logging.Logging;

import java.util.List;

public class ApplicationArguments {
	public static String[] originalArguments;

	public static void parse(String[] args) {
		originalArguments = args;

		for (String arg : args) {
			try {
				String key = arg.split("=")[0].replaceAll("-", "");
				String value = arg.split("=")[1];

				if (key.equals("loggers")) {
					String[] loggers = value.split(",");
					Logging.only(List.of(loggers));
				}
			} catch (Exception error) {}
		}
	}
}
