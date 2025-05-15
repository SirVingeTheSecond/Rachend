package dk.sdu.sem.gamesystem;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("Rachend");
		Game.getInstance().startGame(stage);
	}

	@Override
	public void stop() {
		Game.getInstance().stopGame();
		Platform.exit();
		System.exit(0); // Force exit if threads are still lingering
	}

	public static void main(String[] args) {
		ApplicationArguments.parse(args);
		launch(Main.class);
	}
}