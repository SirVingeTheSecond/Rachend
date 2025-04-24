package dk.sdu.sem.logging;

import java.util.List;

public abstract class FilterMode {
	public abstract boolean accept(String logger);

	public static class Exclude extends FilterMode {
		private final List<String> loggers;

		public Exclude(List<String> loggers) {
			this.loggers = loggers;
		}

		public boolean accept(String logger) {
			return loggers.contains(logger) == false;
		}
	}

	public static class Include extends FilterMode {
		private final List<String> loggers;

		public Include(List<String> loggers) {
			this.loggers = loggers;
		}

		public boolean accept(String logger) {
			return loggers.contains(logger);
		}
	}

	public static class All extends FilterMode {
		public boolean accept(String logger) {
			return true;
		}
	}

	public static class None extends FilterMode {
		public boolean accept(String logger) {
			return false;
		}
	}
}
