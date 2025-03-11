package dk.sdu.sem.gamesystem;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(Main.class);
    }

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("Rachend");
		stage.show();
	}
}