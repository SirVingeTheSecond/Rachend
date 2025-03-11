package dk.sdu.sem.gamesystem;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;

public class Main extends Application {
	private GameLoop gameLoop;

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("Rachend");

		Canvas canvas = new Canvas(800, 600);
		Group root = new Group(canvas);
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.show();

		gameLoop = new GameLoop();
		gameLoop.start();

		GraphicsContext gc = canvas.getGraphicsContext2D();

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
				gameLoop.render(gc);
			}
		};
		renderLoop.start();

		// Handle input
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