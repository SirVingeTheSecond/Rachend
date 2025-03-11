package dk.sdu.sem.gamesystem;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {
	private final Pane gameWindow = new Pane();

    public static void main(String[] args) {
        launch(Main.class);
    }

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("Rachend");

		Scene scene = new Scene(gameWindow);

		stage.setScene(scene);
		stage.show();
	}
}