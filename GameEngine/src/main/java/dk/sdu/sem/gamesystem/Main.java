package dk.sdu.sem.gamesystem;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;
import java.util.*;
import static java.util.stream.Collectors.toList;

import javafx.animation.AnimationTimer;
import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.input.Key;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;

public class Main extends Application {
	private GameLoop gameLoop;
	private Renderer renderer;

	// function to run when the game state is to be updated.

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


		Canvas canvas = new Canvas(800, 600);
		Group root = new Group(canvas);
		Scene scene = new Scene(root);
		setupInputs(scene);
		stage.setScene(scene);
		stage.show();

		gameLoop = new GameLoop();
		gameLoop.start();

		GraphicsContext gc = canvas.getGraphicsContext2D();
		renderer = new Renderer(gc);

		// AnimationTimer for rendering and UI.
		AnimationTimer renderLoop = new AnimationTimer() {
			private double lastNanoTime = Time.getTime();
			@Override
			public void handle(long now) {
				double deltaTime = (now - lastNanoTime) / 1_000_000_000;
				lastNanoTime = now;

				// Update fixed logic (this calls Time.update internally).
				gameLoop.update(deltaTime);

				// Render the current game state.
				renderer.render();

				gameLoop.lateUpdate();

				Input.update();
			}
		};
		renderLoop.start();
	}

	@Override
	public void stop() {
		if (gameLoop != null) {
			gameLoop.stop();
		}
		Platform.exit();
	}

	public static void main(String[] args) {
		launch(Main.class);
	}
}
