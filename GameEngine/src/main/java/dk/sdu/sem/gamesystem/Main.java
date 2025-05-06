package dk.sdu.sem.gamesystem;

import dk.sdu.sem.commonitem.IItemFactory;
import dk.sdu.sem.commonitem.ItemType;
import dk.sdu.sem.commonlevel.ILevelSPI;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.enemy.IEnemyFactory;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.loaders.IAssetLoader;
import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.input.Key;
import dk.sdu.sem.gamesystem.rendering.FXRenderSystem;
import dk.sdu.sem.gamesystem.rendering.IRenderSystem;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import dk.sdu.sem.player.IPlayerFactory;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

public class Main extends Application {
	private static Logging LOGGER = Logging.createLogger("Main", LoggingLevel.DEBUG);

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