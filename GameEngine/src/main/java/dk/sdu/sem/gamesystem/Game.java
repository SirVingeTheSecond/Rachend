package dk.sdu.sem.gamesystem;

import dk.sdu.sem.commonitem.IItemFactory;
import dk.sdu.sem.commonlevel.ILevelSPI;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonsystem.ui.IMenuSPI;
import dk.sdu.sem.enemy.IEnemyFactory;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.input.Key;
import dk.sdu.sem.gamesystem.rendering.FXRenderSystem;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import dk.sdu.sem.player.IPlayerFactory;
import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.ServiceLoader;

public class Game {
	private static Game instance = new Game();
	Logging LOGGER = Logging.createLogger("Game", LoggingLevel.DEBUG);

	private AnimationTimer renderLoop;
	private GameLoop gameLoop;
	private FXRenderSystem renderSystem;
	private Canvas canvas;
	private IMenuSPI menuManager;
	private Stage stage;

	private Game() {

	}

	public static Game getInstance() {
		return instance;
	}

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
			LOGGER.debug("ON MOUSE CLICKED " + (event.getButton() == MouseButton.PRIMARY));
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

	/**
	 * Sets up the game world.
	 */
	private void setupGameWorld() {
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

		LOGGER.debug("Game world setup complete with map, player, enemy, and items");
	}

	public void createGameView(Stage stage) {
		float baseWidth = GameConstants.WORLD_SIZE.x() * GameConstants.TILE_SIZE;
		float baseHeight = GameConstants.WORLD_SIZE.y() * GameConstants.TILE_SIZE;

		canvas = new Canvas(baseWidth, baseHeight);
		Group canvasGroup = new Group(canvas);
		StackPane root = new StackPane(canvasGroup);
		root.setStyle("-fx-background-color: black;");

		Scene gameScene = new Scene(root, baseWidth, baseHeight);
		gameScene.setCursor(Cursor.NONE);

		// Bind scale properties while maintaining aspect ratio
		canvas.scaleXProperty().bind(Bindings.createDoubleBinding(
			() -> Math.min(gameScene.getWidth() / baseWidth, gameScene.getHeight() / baseHeight),
			gameScene.widthProperty(), gameScene.heightProperty()
		));
		canvas.scaleYProperty().bind(canvas.scaleXProperty()); // Keep proportions

		// Center the canvas dynamically
		canvas.layoutXProperty().bind(gameScene.widthProperty().subtract(baseWidth).divide(2));
		canvas.layoutYProperty().bind(gameScene.heightProperty().subtract(baseHeight).divide(2));

		stage.setScene(gameScene);

		stage.show();
	}

	public void startGame(Stage stage) {
		try {
			this.stage = stage;
			createGameView(stage);

			setupInputs(canvas.getScene());

			// IMPORTANT: Init assets BEFORE creating any game entities
			initializeAssets();

			gameLoop = new GameLoop();
			gameLoop.start();

			// Get renderer
			GraphicsContext gc = canvas.getGraphicsContext2D();
			renderSystem = FXRenderSystem.getInstance();
			renderSystem.initialize(gc);

			// For rendering and UI
			renderLoop = new AnimationTimer() {
				private double lastNanoTime = System.nanoTime();

				@Override
				public void handle(long now) {
					double deltaTime = (now - lastNanoTime) / 1_000_000_000.0;
					lastNanoTime = now;

					if (gameLoop == null || Time.getTimeScale() == 0)
						return;

					gameLoop.update(deltaTime);
					gameLoop.lateUpdate();

					renderSystem.lateUpdate(); // Not adhering to architecture, I know

					gameLoop.guiUpdate(gc);

					Input.update();
				}
			};

			menuManager = ServiceLoader.load(IMenuSPI.class).findFirst().orElse(null);
			if (menuManager != null) {
				menuManager.showMainMenu(stage);
				stopGame();
			}

			renderLoop.start();

		} catch (Throwable t) {
			LOGGER.error("Application start failed:");
			t.printStackTrace();
		}
	}

	/**
	 * Initialize the asset system.
	 */
	private void initializeAssets() {
		// Init the asset system - will load all providers automatically
		AssetFacade.initialize();

		// Preload floor as a sprite sheet
		AssetFacade.preloadAsType("floor", SpriteMap.class);

		LOGGER.debug("Asset system initialized.");
	}

	//Restarts the game
	public void restart() {
		//Restart gameloop to send start events
		if (gameLoop != null)
			gameLoop.stop();

		gameLoop = new GameLoop();
		gameLoop.start();

		//Restart scenemanager
		SceneManager.getInstance().restart();

		renderSystem.clear();

		//Setup world again
		setupGameWorld();
		Time.setTimeScale(1);
	}

	public void stopGame() {
		paused = false;
		Time.setTimeScale(0);
		if (gameLoop != null)
			gameLoop.stop();

		gameLoop = null;

		SceneManager.getInstance().restart();

		renderSystem.clear();
	}

	boolean paused = false;
	double prevScale;
	void togglePause() {
		if (gameLoop == null)
			return;

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
		if (menuManager != null)
			menuManager.showPauseMenu(stage);
	}

	public void unpauseGame() {
		Time.setTimeScale(prevScale);
		paused = false;
		menuManager.hidePauseMenu(stage);
	}
}
