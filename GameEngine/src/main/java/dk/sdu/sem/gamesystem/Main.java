package dk.sdu.sem.gamesystem;
import dk.sdu.sem.gamesystem.services.IProcessor;
import java.util.*;
import static java.util.stream.Collectors.toList;

public class Main {
	private static long deltaTime = 0;
	private long currentTime = 0;

	public static long getDeltatime() {
		return deltaTime;
	}

	private static Collection<? extends IProcessor> getProcessors() {
		return ServiceLoader.load(IProcessor.class).stream().map(ServiceLoader.Provider::get).collect(toList());
	}
	// function to run when the game state is to be updated.
	public static void update() {
		for (IProcessor processorImplimentation : getProcessors()) {
			processorImplimentation.process();
		}
	}

	public static void main(String[] args) {
		long startTime = System.nanoTime();
		// loop actions start
		update();
		// loop actions end
		long currentTime = System.nanoTime();
		long deltaTime = startTime - currentTime;
	}
}