package dk.sdu.sem.uisystem;

import dk.sdu.sem.commonsystem.ui.IMenuSPI;
import dk.sdu.sem.gamesystem.Game;
import dk.sdu.sem.gamesystem.GameConstants;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Random;

public class MenuManager implements IMenuSPI {
	private double baseWidth = GameConstants.WORLD_SIZE.x() * GameConstants.TILE_SIZE;
	private double baseHeight = GameConstants.WORLD_SIZE.y() * GameConstants.TILE_SIZE;
	private StackPane startMenu;
	private StackPane pauseOverlay;
	private final Random random = new Random();
	private StackPane gameOverOverlay;

	@Override
	public void showMainMenu(Stage stage) {
		try {
			Scene scene = stage.getScene();

			if (startMenu == null) {
				StackPane root = (StackPane)scene.getRoot();

				stage.setWidth(baseWidth);
				stage.setHeight(baseHeight);

				startMenu = new StackPane();
				Image image = new Image(MenuManager.class.getResourceAsStream("/background.png"));

				root.getChildren().add(startMenu);

				startMenu.setBackground(new Background(
					new BackgroundImage(image, null, null, null, new BackgroundSize(0,0,false,false,true,true))
				));

				VBox vbox = new VBox();
				vbox.setAlignment(Pos.CENTER);
				vbox.setSpacing(10);

				ImageView view = new ImageView(new Image(MenuManager.class.getResourceAsStream("/title_text.png")));
				view.setPreserveRatio(true);
				view.setFitWidth(baseWidth - 50);
				VBox.setMargin(view, new Insets(0, 0, -160, 0));
				vbox.getChildren().add(view);

				//region Start button
				Button startButton = createButton(new Image(MenuManager.class.getResourceAsStream("/start_button.png")));
				vbox.getChildren().add(startButton);

				startButton.setOnAction(event -> {
					Game.getInstance().restart();
					startMenu.setVisible(false);
					scene.setCursor(Cursor.NONE);
				});
				//endregion

				//region Options button
				Button optionsButton = createButton(new Image(MenuManager.class.getResourceAsStream("/options_button.png")));
				vbox.getChildren().add(optionsButton);
				//endregion

				//region Quit button
				Button quitButton = createButton(new Image(MenuManager.class.getResourceAsStream("/quit_button.png")));
				vbox.getChildren().add(quitButton);

				quitButton.setOnAction(event -> {
					Platform.exit();
				});
				//endregion

				Group scalingGroup = new Group(vbox);
				StackPane.setAlignment(scalingGroup, Pos.CENTER);

				startMenu.getChildren().add(scalingGroup);

				Platform.runLater(() -> {
					DoubleBinding scaleBinding = Bindings.createDoubleBinding(() -> {
						double contentWidth = scalingGroup.getLayoutBounds().getWidth();
						double contentHeight = scalingGroup.getLayoutBounds().getHeight();
						double scaleX = scene.getWidth() / contentWidth;
						double scaleY = scene.getHeight() / contentHeight;
						return Math.min(scaleX, scaleY);
					}, scene.widthProperty(), scene.heightProperty());

					scalingGroup.scaleXProperty().bind(scaleBinding);
					scalingGroup.scaleYProperty().bind(scaleBinding);
				});

				GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
				AffineTransform affine = gc.getDefaultTransform();

				Light.Point l1 = new Light.Point();
				l1.xProperty().bind(scalingGroup.scaleXProperty().multiply(125 * affine.getScaleX()));
				l1.yProperty().bind(scalingGroup.scaleYProperty().multiply(280 * affine.getScaleY()));
				l1.setZ(5);
				l1.setColor(new Color(1,0.8,0.7,1));

				Light.Point l2 = new Light.Point();
				l2.xProperty().bind(scalingGroup.scaleXProperty().multiply(585 * affine.getScaleX()));
				l2.yProperty().bind(scalingGroup.scaleYProperty().multiply(280 * affine.getScaleY()));
				l2.setZ(5);
				l2.setColor(new Color(1,0.8,0.7,1));

				AnimationTimer timer = new AnimationTimer() {
					private double currentFlicker1 = 1; // Current flicker intensity for light 1
					private double currentFlicker2 = 1; // Current flicker intensity for light 2
					private double targetFlicker1 = 1; // Target flicker intensity for light 1
					private double targetFlicker2 = 1; // Target flicker intensity for light 2
					final double flickerSpeed = 0.03; // Speed of the flicker
					final double flickerAmplitude = 1.5; // Amplitude of the flicker (how much it flickers)
					final double flickerSmoothness = 0.01; // Smoothing factor for gradual transition

					@Override
					public void handle(long l) {
						if (random.nextDouble() < flickerSpeed) {
							targetFlicker1 = 1 + (random.nextDouble() - 0.5) * flickerAmplitude;
						}

						if (random.nextDouble() < flickerSpeed) {
							targetFlicker2 = 1 + (random.nextDouble() - 0.5) * flickerAmplitude;
						}

						// Smoothly interpolate towards the new flicker target value
						currentFlicker1 += (targetFlicker1 - currentFlicker1) * flickerSmoothness;
						currentFlicker2 += (targetFlicker2 - currentFlicker2) * flickerSmoothness;

						// Clamp the values between 0 and 1 to avoid invalid z-pos
						currentFlicker1 = Math.max(0, Math.min(1, currentFlicker1));
						currentFlicker2 = Math.max(0, Math.min(1, currentFlicker2));

						l1.setZ(120 * currentFlicker1 * scalingGroup.getScaleX() * affine.getScaleX());
						l2.setZ(120 * currentFlicker2 * scalingGroup.getScaleX() * affine.getScaleX());
					}
				};

				Lighting light1 = new Lighting();
				light1.setLight(l1);

				Lighting light2 = new Lighting();
				light2.setLight(l2);

				Blend blend = new Blend(BlendMode.ADD);
				blend.setTopInput(light1);
				blend.setBottomInput(light2);

				view.setEffect(blend);

				timer.start();
			}

			startMenu.setVisible(true);
			scene.setCursor(Cursor.DEFAULT);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private Button createButton(Image image) {
		ImageView startImage = new ImageView(image);
		startImage.setPreserveRatio(true);
		startImage.setFitWidth(baseWidth / 2f);

		Button button = new Button();
		button.setGraphic(startImage);
		button.setStyle("-fx-focus-color: transparent; -fx-background-color: transparent;");

		// Hover effects
		ColorAdjust colorAdjust = new ColorAdjust();
		startImage.setEffect(colorAdjust);

		button.setOnMouseEntered(e -> colorAdjust.setInput(new Glow(10))); // brighter on hover
		button.setOnMouseExited(e -> colorAdjust.setInput(null)); // back to normal

		return button;
	}

	@Override
	public void showPauseMenu(Stage stage) {
		Scene scene = stage.getScene();

		if (pauseOverlay == null) {
			StackPane root = (StackPane)scene.getRoot();

			pauseOverlay = new StackPane();

			root.getChildren().add(pauseOverlay);

			VBox vbox = new VBox();
			vbox.setAlignment(Pos.CENTER);
			vbox.setSpacing(10);

			//region Start button
			Button resumeButton = createButton(new Image(MenuManager.class.getResourceAsStream("/resume_button.png")));
			vbox.getChildren().add(resumeButton);

			resumeButton.setOnAction(event -> {
				Game.getInstance().unpauseGame();
				hidePauseMenu(stage);
			});
			//endregion

			//region Quit button
			Button quitButton = createButton(new Image(MenuManager.class.getResourceAsStream("/quit_button.png")));
			vbox.getChildren().add(quitButton);

			quitButton.setOnAction(event -> {
				Game.getInstance().stopGame();
				hidePauseMenu(stage);
				showMainMenu(stage);
			});
			//endregion

			Group scalingGroup = new Group(vbox);
			StackPane.setAlignment(scalingGroup, Pos.CENTER);

			pauseOverlay.getChildren().add(scalingGroup);

			Platform.runLater(() -> {
				DoubleBinding scaleBinding = Bindings.createDoubleBinding(() -> {
					double contentWidth = scalingGroup.getLayoutBounds().getWidth();
					double contentHeight = scalingGroup.getLayoutBounds().getHeight();
					double scaleX = scene.getWidth() / contentWidth;
					double scaleY = scene.getHeight() / contentHeight;
					return Math.min(scaleX * 0.5, scaleY * 0.5);
				}, scene.widthProperty(), scene.heightProperty());

				scalingGroup.scaleXProperty().bind(scaleBinding);
				scalingGroup.scaleYProperty().bind(scaleBinding);
			});
		}

		blurBackground();

		pauseOverlay.setVisible(true);
		scene.setCursor(Cursor.DEFAULT);
	}

	@Override
	public void hidePauseMenu(Stage stage) {
		Scene scene = stage.getScene();
		if (pauseOverlay != null) {
			pauseOverlay.setVisible(false);
		}
		Game.getInstance().getCanvas().setEffect(null);
		scene.setCursor(Cursor.NONE);
	}

	@Override
	public void showGameOverMenu(Stage stage) {
		Scene scene = stage.getScene();

		if (gameOverOverlay == null) {
			StackPane root = (StackPane)scene.getRoot();

			gameOverOverlay = new StackPane();

			root.getChildren().add(gameOverOverlay);

			VBox vbox = new VBox();
			vbox.setAlignment(Pos.CENTER);
			vbox.setSpacing(10);

			//region Game Over Text
			ImageView view = new ImageView(new Image(MenuManager.class.getResourceAsStream("/game_over_text.png")));
			view.setPreserveRatio(true);
			view.setFitWidth(baseWidth - 50);
			VBox.setMargin(view, new Insets(0, 0, -220, 0));
			vbox.getChildren().add(view);
			//endregion

			//region Restart button
			Button restartButton = createButton(new Image(MenuManager.class.getResourceAsStream("/restart_button.png")));
			vbox.getChildren().add(restartButton);

			restartButton.setOnAction(event -> {
				Game.getInstance().restart();
				hideGameOverMenu(stage);
				scene.setCursor(Cursor.NONE);
			});
			//endregion

			//region Quit button
			Button quitButton = createButton(new Image(MenuManager.class.getResourceAsStream("/quit_button.png")));
			vbox.getChildren().add(quitButton);

			quitButton.setOnAction(event -> {
				Game.getInstance().stopGame();
				hideGameOverMenu(stage);
				showMainMenu(stage);
			});
			//endregion

			Group scalingGroup = new Group(vbox);
			StackPane.setAlignment(scalingGroup, Pos.CENTER);

			gameOverOverlay.getChildren().add(scalingGroup);

			Platform.runLater(() -> {
				DoubleBinding scaleBinding = Bindings.createDoubleBinding(() -> {
					double contentWidth = scalingGroup.getLayoutBounds().getWidth();
					double contentHeight = scalingGroup.getLayoutBounds().getHeight();
					double scaleX = scene.getWidth() / contentWidth;
					double scaleY = scene.getHeight() / contentHeight;
					return Math.min(scaleX * 0.7, scaleY * 0.7);
				}, scene.widthProperty(), scene.heightProperty());

				scalingGroup.scaleXProperty().bind(scaleBinding);
				scalingGroup.scaleYProperty().bind(scaleBinding);
			});
		}

		blurBackground();

		gameOverOverlay.setVisible(true);
		scene.setCursor(Cursor.DEFAULT);
	}

	@Override
	public void hideGameOverMenu(Stage stage) {
		Scene scene = stage.getScene();
		if (gameOverOverlay != null) {
			gameOverOverlay.setVisible(false);
		}

		Game.getInstance().getCanvas().setEffect(null);

		scene.setCursor(Cursor.NONE);
	}

	private void blurBackground() {
		BoxBlur blur = new BoxBlur(5, 5, 3);

		Game.getInstance().getCanvas().setEffect(blur);
	}
}
