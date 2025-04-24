package dk.sdu.sem.gamesystem;

import dk.sdu.sem.commonitem.IItemFactory;
import dk.sdu.sem.commonlevel.ILevelSPI;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.enemy.IEnemyFactory;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.loaders.IAssetLoader;
import dk.sdu.sem.gamesystem.rendering.FXRenderSystem;
import dk.sdu.sem.gamesystem.rendering.IRenderSystem;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.player.IPlayerFactory;
import dk.sdu.sem.commonsystem.Vector2D;
import javafx.animation.AnimationTimer;
import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.input.Key;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.ServiceLoader;

public class Main extends Application {
	private static Main instance;

	private GameLoop gameLoop;
	private AnimationTimer renderLoop;
	private IRenderSystem renderSystem;

	private final double baseWidth = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x();
	private final double baseHeight = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y();

	private MenuManager menuManager;

	private Canvas canvas;

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
				case R:
					if (event.isAltDown())
						restart();
					break;
				case ESCAPE:
					togglePause();
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

		scene.setOnMousePressed(event -> {
			System.out.println("ON MOUSE CLICKED " + (event.getButton() == MouseButton.PRIMARY));
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
			Point2D p = canvas.sceneToLocal(event.getSceneX(), event.getSceneY());

			Input.setMousePosition(new Vector2D(
				(float)(p.getX()),
				(float)(p.getY())
			));
		});

		scene.setOnMouseDragged(event -> {
			Point2D p = canvas.sceneToLocal(event.getSceneX(), event.getSceneY());

			Input.setMousePosition(new Vector2D(
				(float)(p.getX()),
				(float)(p.getY())
			));
		});
	}

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("Rachend");
		instance = this;
		menuManager = new MenuManager(stage, baseWidth, baseHeight);
		menuManager.showMainMenu();
	}

	void startGame(Stage stage) {
		try {
			canvas = menuManager.showGameView();

			setupInputs(canvas.getScene());

			// IMPORTANT: Init assets BEFORE creating any game entities
			initializeAssets();

			// Init game loop
			gameLoop = new GameLoop();
			gameLoop.start();

			// Get renderer
			GraphicsContext gc = canvas.getGraphicsContext2D();
			renderSystem = FXRenderSystem.getInstance();
			renderSystem.initialize(gc);

			// Now init the game world after assets are loaded
			setupGameWorld();

			// For rendering and UI
			renderLoop = new AnimationTimer() {
				private double lastNanoTime = System.nanoTime();

				@Override
				public void handle(long now) {
					double deltaTime = (now - lastNanoTime) / 1_000_000_000.0;
					lastNanoTime = now;

					if (Time.getTimeScale() == 0)
						return;

					gameLoop.update(deltaTime);
					gameLoop.lateUpdate();

					renderSystem.lateUpdate(); // Not adhering to architecture, I know

					gameLoop.guiUpdate(gc);

					Input.update();
				}
			};

			renderLoop.start();
		} catch (Throwable t) {
			System.err.println("Application start failed:");
			t.printStackTrace(System.err);
			throw t;
		}
	}

	//Restarts the game
	private void restart() {
		//Restart gameloop to send start events
		gameLoop.stop();
		gameLoop = new GameLoop();
		gameLoop.start();

		//Restart scenemanager
		SceneManager.getInstance().restart();

		renderSystem.clear();

		//Setup world again
		setupGameWorld();
	}


	void stopGame() {
		unpauseGame();
		Time.setTimeScale(1);
		gameLoop.stop();
		renderLoop.stop();

		SceneManager.getInstance().restart();

		renderSystem.clear();
	}

	boolean paused = false;
	double prevScale;
	void togglePause() {
		if (paused) {
			unpauseGame();
		}
		else {
			pauseGame();
		}
	}

	void pauseGame() {
		prevScale = Time.getTimeScale();
		Time.setTimeScale(0);
		paused = true;
		menuManager.showPauseScreen();
	}

	void unpauseGame() {
		Time.setTimeScale(prevScale);
		paused = false;
		menuManager.hidePauseScreen();
	}

	/*
	private void showPauseScreen() {
		pauseOverlay.setVisible(true);
		canvas.getScene().setCursor(Cursor.DEFAULT);
	}*/

	/**
	 * Sets up the game world.
	 */
	private void setupGameWorld() {
		/*
		// Create tilemap
		TilemapFactory tileMapFactory = ServiceLocator.getEntityFactory(TilemapFactory.class);
		if (tileMapFactory == null) {
			tileMapFactory = new TilemapFactory();
		}
		Entity tilemap = tileMapFactory.create();
		*/
		//ServiceLoader.load(IRoomSPI.class).findFirst().ifPresent(spi -> SceneManager.getInstance().setActiveScene(spi.createRoom(true, false, true, false)));

		ServiceLoader.load(ILevelSPI.class).findFirst().ifPresent(spi -> spi.generateLevel(8,12, 10, 10));

		// We should consider renaming Scene to something like "GameScene"
		dk.sdu.sem.commonsystem.Scene activeScene = SceneManager.getInstance().getActiveScene();

		// Create player
		Optional<IPlayerFactory> playerFactoryOpt = ServiceLoader.load(IPlayerFactory.class).findFirst();
		if (playerFactoryOpt.isEmpty()) {
			throw new RuntimeException("No IPlayerFactory implementation found");
		}
		IPlayerFactory playerFactory = playerFactoryOpt.get();
		Entity player = playerFactory.create();

		// Create enemy
		Optional<IEnemyFactory> enemyFactoryOpt = ServiceLoader.load(IEnemyFactory.class).findFirst();
		if (enemyFactoryOpt.isEmpty()) {
			throw new RuntimeException("No IEnemyFactory implementation found");
		}
		IEnemyFactory enemyFactory = enemyFactoryOpt.get();
		Entity enemy = enemyFactory.create();

		// Create item factory
		Optional<IItemFactory> itemFactoryOpt = ServiceLoader.load(IItemFactory.class).findFirst();
		if (itemFactoryOpt.isEmpty()) {
			throw new RuntimeException("No IItemFactory implementation found");
		}
		IItemFactory itemFactory = itemFactoryOpt.get();

		// Create collectible items
		Entity coin1 = itemFactory.createCoin(new Vector2D(100, 100));
		Entity coin2 = itemFactory.createCoin(new Vector2D(400, 200));
		Entity coin3 = itemFactory.createCoin(new Vector2D(300, 400));
		Entity healthPotion = itemFactory.createHealthPotion(new Vector2D(500, 350));

		// Add entities to scene
		activeScene.addEntity(player);
		activeScene.addPersistedEntity(player);
		activeScene.addEntity(enemy);

		// Add item entities
		activeScene.addEntity(coin1);
		activeScene.addEntity(coin2);
		activeScene.addEntity(coin3);
		activeScene.addEntity(healthPotion);

		System.out.println("Game world setup complete with map, player, enemy, and items");
	}

	/**
	 * Initialize the asset system.
	 */
	private void initializeAssets() {
		// Init the asset system - will load all providers automatically
		AssetFacade.initialize();

		// Preload floor as a sprite sheet
		AssetFacade.preloadAsType("floor", SpriteMap.class);

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

	public static Main getInstance() {
		return instance;
	}

	@Override
	public void stop() {
		if (renderLoop != null) {
			renderLoop.stop();
		}
		if (gameLoop != null) {
			gameLoop.stop();
		}
		Platform.exit();
		System.exit(0); // Force exit if threads are still lingering
	}

	public static void main(String[] args) {
		launch(Main.class);
	}
}