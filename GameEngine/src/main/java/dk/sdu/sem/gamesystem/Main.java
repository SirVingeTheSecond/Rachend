package dk.sdu.sem.gamesystem;

import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.input.Key;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {
	private final Pane gameWindow = new Pane();

    public static void main(String[] args) {
        launch(Main.class);
    }

	private void setupInputs(Scene scene) {
		scene.setOnKeyPressed(event -> {
			switch (event.getCode()) {
				case UP:
					Input.setKeyPressed(Key.UP, true);
					break;
				case DOWN:
					Input.setKeyPressed(Key.DOWN, true);
					break;
				case LEFT:
					Input.setKeyPressed(Key.LEFT, true);
					break;
				case RIGHT:
					Input.setKeyPressed(Key.RIGHT, true);
					break;
			}
		});

		scene.setOnKeyReleased(event -> {
			switch (event.getCode()) {
				case UP:
					Input.setKeyPressed(Key.UP, false);
					break;
				case DOWN:
					Input.setKeyPressed(Key.DOWN, false);
					break;
				case LEFT:
					Input.setKeyPressed(Key.LEFT, false);
					break;
				case RIGHT:
					Input.setKeyPressed(Key.RIGHT, false);
					break;
			}
		});

		scene.setOnMouseClicked(event -> {
			switch (event.getButton()) {
				case PRIMARY:
					Input.setKeyPressed(Key.MOUSE1, true);
					break;
				case SECONDARY:
					Input.setKeyPressed(Key.MOUSE2, true);
					break;
			}
		});

		scene.setOnMouseReleased(event -> {
			switch (event.getButton()) {
				case PRIMARY:
					Input.setKeyPressed(Key.MOUSE1, false);
					break;
				case SECONDARY:
					Input.setKeyPressed(Key.MOUSE2, false);
					break;
			}
		});
	}

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("Rachend");

		Scene scene = new Scene(gameWindow);

		setupInputs(scene);

		stage.setScene(scene);
		stage.show();
	}
}