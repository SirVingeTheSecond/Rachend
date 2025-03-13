package dk.sdu.sem.gamesystem;
import dk.sdu.sem.gamesystem.services.IProcessor;
import java.util.*;
import static java.util.stream.Collectors.toList;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {
	private final Pane gameWindow = new Pane();
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

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("Rachend");

		Scene scene = new Scene(gameWindow);

		stage.setScene(scene);
		stage.show();
		}
	public static void main(String[] args) {
		long startTime = System.nanoTime();
		launch(Main.class);
		// loop actions start
		update();
		long currentTime = System.nanoTime();
		long deltaTime = startTime - currentTime;
		// loop actions end
	}
}
