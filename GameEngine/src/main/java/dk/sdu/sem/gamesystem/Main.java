package dk.sdu.sem.gamesystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.loaders.IAssetLoader;
import dk.sdu.sem.gamesystem.factories.TilemapFactory;
import dk.sdu.sem.gamesystem.rendering.FXRenderSystem;
import dk.sdu.sem.gamesystem.rendering.IRenderSystem;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.player.IPlayerFactory;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import dk.sdu.sem.gamesystem.services.IStart;
import javafx.animation.AnimationTimer;
import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.input.Key;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;

import java.util.ServiceLoader;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

public class Main extends Application {
	private GameLoop gameLoop;
	private IRenderSystem renderSystem;

	private final Set<IGUIUpdate> guiUpdates = new HashSet<>();

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

		scene.setOnMouseMoved(event -> {
			Input.setMousePosition(new Vector2D((float)event.getSceneX(), (float)event.getSceneY()));
		});
	}

	@Override
	public void start(Stage stage) throws Exception {
		ServiceLoader.load(IGUIUpdate.class).forEach(guiUpdates::add);

		stage.setTitle("Rachend");

		Canvas canvas = new Canvas(800, 600);
		Group root = new Group(canvas);
		Scene scene = new Scene(root);

		scene.setCursor(Cursor.NONE);

		setupInputs(scene);
		stage.setScene(scene);
		stage.show();

		// IMPORTANT: Initialize assets BEFORE creating any game entities
		initializeAssets();

		// Init game loop
		gameLoop = new GameLoop();
		gameLoop.start();

		// Get renderer
		GraphicsContext gc = canvas.getGraphicsContext2D();
		renderSystem = FXRenderSystem.getInstance();
		renderSystem.initialize(gc);

		// Now set up the game world after assets are loaded
		setupGameWorld();

		// For rendering and UI
		AnimationTimer renderLoop = new AnimationTimer() {
			private double lastNanoTime = System.nanoTime();

			@Override
			public void handle(long now) {
				double deltaTime = (now - lastNanoTime) / 1_000_000_000.0;
				lastNanoTime = now;

				Time.update(deltaTime);
				gameLoop.update(deltaTime);
				gameLoop.lateUpdate();

				renderSystem.lateUpdate(); // Not adhering to architecture, I know

				guiUpdates.forEach(gui -> gui.onGUI(gc));

				Input.update();
			}
		};
		renderLoop.start();
	}

	/**
	 * Sets up the game world with a tilemap and player entity.
	 */
	private void setupGameWorld() {
		dk.sdu.sem.commonsystem.Scene activeScene = SceneManager.getInstance().getActiveScene();

		TilemapFactory tileMapFactory = ServiceLocator.getEntityFactory(TilemapFactory.class);
		if (tileMapFactory == null) {
			tileMapFactory = new TilemapFactory();
		}

		Entity tilemap = tileMapFactory.create();

		IPlayerFactory playerFactory = ServiceLocator.getPlayerFactory();
		if (playerFactory == null) {
			throw new RuntimeException("No IPlayerFactory implementation found");
		}

		Entity player = playerFactory.create();

		activeScene.addEntity(tilemap);
		activeScene.addEntity(player);
	}

	/**
	 * Initialize the asset system.
	 */
	private void initializeAssets() {
		// Init the asset system - will load all providers automatically
		AssetFacade.initialize();

		// Preload essential assets using direct file names - much simpler!
		AssetFacade.preload("floor");

		// Preload player animations
		AssetFacade.preload("player_idle");
		AssetFacade.preload("player_run");

		System.out.println("Asset system initialized.");
	}

	private void debugAssetLoaders() {
		System.out.println("=== Available Asset Loaders ===");
		ServiceLoader.load(IAssetLoader.class).forEach(loader -> {
			System.out.println(" - " + loader.getClass().getSimpleName() +
				" for type " + loader.getAssetType().getSimpleName());
		});
		System.out.println("==============================");
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