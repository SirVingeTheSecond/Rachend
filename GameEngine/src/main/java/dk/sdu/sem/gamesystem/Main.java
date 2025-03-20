package dk.sdu.sem.gamesystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.components.TileMapComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.rendering.*;
import dk.sdu.sem.player.PlayerComponent;
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

import java.util.Random;

public class Main extends Application {
	private GameLoop gameLoop;
	private RenderSystem renderSystem;

	private void setupInputs(Scene scene) {
		scene.setOnKeyPressed(event -> {
			switch (event.getCode()) {
				case W:
					Input.setKeyPressed(Key.UP, true);
					break;
				case S:
					Input.setKeyPressed(Key.DOWN, true);
					break;
				case A:
					Input.setKeyPressed(Key.LEFT, true);
					break;
				case D:
					Input.setKeyPressed(Key.RIGHT, true);
					break;
				case SPACE:
					Input.setKeyPressed(Key.SPACE, true);
					break;
			}
		});

		scene.setOnKeyReleased(event -> {
			switch (event.getCode()) {
				case W:
					Input.setKeyPressed(Key.UP, false);
					break;
				case S:
					Input.setKeyPressed(Key.DOWN, false);
					break;
				case A:
					Input.setKeyPressed(Key.LEFT, false);
					break;
				case D:
					Input.setKeyPressed(Key.RIGHT, false);
					break;
				case SPACE:
					Input.setKeyPressed(Key.SPACE, false);
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
		gc.setImageSmoothing(false);

		renderSystem = new RenderSystem(gc);

		ResourceManager resourceManager = ResourceManager.getInstance();

		// Load tileset
		SpriteMap floorSheet = resourceManager.createSpriteSheet("floor", "floor.png");
		floorSheet.defineSpritesFromGrid(3, 4, 16, 16);

		TileSet floorTiles = resourceManager.createTileSet("floor", "floor");
		floorTiles.defineAllTilesFromGrid(3, 4);

		Entity tilemapEntity = new Entity();
		tilemapEntity.addComponent(new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1)));

		// Create tilemap data (25x19 grid of random tiles)
		int[][] tileData = new int[25][19];
		Random rand = new Random(1);
		for (int x = 0; x < 25; x++) {
			for (int y = 0; y < 19; y++) {
				tileData[x][y] = rand.nextInt(12); // Random tile between 0-11
			}
		}

		tilemapEntity.addComponent(new TileMapComponent(floorTiles, tileData, 32));
		dk.sdu.sem.commonsystem.Scene.getActiveScene().addEntity(tilemapEntity);

		Entity entity = new Entity();
		entity.addComponent(new TransformComponent(new Vector2D(200, 200), 0, new Vector2D(1, 1)));
		entity.addComponent(new PhysicsComponent(5));
		entity.addComponent(new PlayerComponent(1000));

		Sprite playerSprite = new Sprite("player", resourceManager.loadImage("player.png"));
		entity.addComponent(new SpriteRendererComponent(playerSprite));

		dk.sdu.sem.commonsystem.Scene.getActiveScene().addEntity(entity);

		// For rendering and UI
		AnimationTimer renderLoop = new AnimationTimer() {
			private double lastNanoTime = System.nanoTime();

			@Override
			public void handle(long now) {
				double deltaTime = (now - lastNanoTime) / 1_000_000_000.0;
				lastNanoTime = now;

				// Update time and game state
				Time.update(deltaTime);
				gameLoop.update(deltaTime);
				gameLoop.lateUpdate();

				// Render the current game state
				renderSystem.render();

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