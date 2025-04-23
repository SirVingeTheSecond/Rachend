package dk.sdu.sem.gamesystem;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Random;
import java.util.Stack;

public class MenuManager {
	private Stage stage;
	private double baseWidth, baseHeight;
	private Scene startScene;
	private Scene gameScene;
	private double windowWidth = baseWidth;
	private double windowHeight = baseHeight;
	private Canvas canvas;
	private VBox pauseOverlay;
	private final Random random = new Random();

	public MenuManager(Stage stage, double baseWidth, double baseHeight) {
		this.stage = stage;
		this.baseWidth = baseWidth;
		this.baseHeight = baseHeight;
	}

	public void showMainMenu() {
		try {
			if (startScene == null) {
				stage.setWidth(baseWidth);
				stage.setHeight(baseHeight);

				StackPane root = new StackPane();
				Image image = new Image(MenuManager.class.getResourceAsStream("/background.png"));

				startScene = new Scene(root, baseWidth, baseHeight);
				root.setBackground(new Background(
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
					Main.getInstance().startGame(stage);
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
					Main.getInstance().stop();
				});
				//endregion

				Group scalingGroup = new Group(vbox);
				StackPane.setAlignment(scalingGroup, Pos.CENTER);

				root.getChildren().add(scalingGroup);

				Platform.runLater(() -> {
					DoubleBinding scaleBinding = Bindings.createDoubleBinding(() -> {
						double contentWidth = scalingGroup.getLayoutBounds().getWidth();
						double contentHeight = scalingGroup.getLayoutBounds().getHeight();
						double scaleX = startScene.getWidth() / contentWidth;
						double scaleY = startScene.getHeight() / contentHeight;
						return Math.min(scaleX, scaleY);
					}, startScene.widthProperty(), startScene.heightProperty());

					scalingGroup.scaleXProperty().bind(scaleBinding);
					scalingGroup.scaleYProperty().bind(scaleBinding);
				});

				Light.Point l1 = new Light.Point();
				l1.xProperty().bind(scalingGroup.scaleXProperty().multiply(125));
				l1.yProperty().bind(scalingGroup.scaleYProperty().multiply(280));
				l1.setZ(5);
				l1.setColor(new Color(1,0.8,0.7,1));

				Light.Point l2 = new Light.Point();
				l2.xProperty().bind(scalingGroup.scaleXProperty().multiply(585));
				l2.yProperty().bind(scalingGroup.scaleYProperty().multiply(280));
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

						l1.setZ(80 * currentFlicker1 * scalingGroup.getScaleX());
						l2.setZ(80 * currentFlicker2 * scalingGroup.getScaleX());
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

			windowWidth = stage.getWidth();
			windowHeight = stage.getHeight();

			stage.setScene(startScene);

			stage.setWidth(windowWidth);
			stage.setHeight(windowHeight);

			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Canvas showGameView() {
		if (canvas == null) {
			canvas = new Canvas(baseWidth, baseHeight);
			Group canvasGroup = new Group(canvas);
			StackPane root = new StackPane(canvasGroup);
			root.setStyle("-fx-background-color: black;");

			gameScene = new Scene(root, baseWidth, baseHeight);
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
		}

		windowWidth = stage.getWidth();
		windowHeight = stage.getHeight();

		stage.setScene(gameScene);

		stage.setWidth(windowWidth);
		stage.setHeight(windowHeight);

		stage.show();

		return canvas;
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

	public void showPauseScreen() {
		if (pauseOverlay == null) {
			StackPane root = (StackPane)gameScene.getRoot();

			pauseOverlay = new VBox();
			pauseOverlay.setAlignment(Pos.CENTER);
			pauseOverlay.setSpacing(10);

			//region Start button
			Button resumeButton = createButton(new Image(MenuManager.class.getResourceAsStream("/resume_button.png")));
			pauseOverlay.getChildren().add(resumeButton);

			resumeButton.setOnAction(event -> {
				Main.getInstance().unpauseGame();
				pauseOverlay.setVisible(false);
			});
			//endregion

			//region Quit button
			Button quitButton = createButton(new Image(MenuManager.class.getResourceAsStream("/quit_button.png")));
			pauseOverlay.getChildren().add(quitButton);

			quitButton.setOnAction(event -> {
				Main.getInstance().stopGame();
				pauseOverlay.setVisible(false);
				showMainMenu();
			});
			//endregion

			Group scalingGroup = new Group(pauseOverlay);
			StackPane.setAlignment(scalingGroup, Pos.CENTER);

			root.getChildren().add(scalingGroup);

			Platform.runLater(() -> {
				DoubleBinding scaleBinding = Bindings.createDoubleBinding(() -> {
					double contentWidth = scalingGroup.getLayoutBounds().getWidth();
					double contentHeight = scalingGroup.getLayoutBounds().getHeight();
					double scaleX = gameScene.getWidth() / contentWidth;
					double scaleY = gameScene.getHeight() / contentHeight;
					return Math.min(scaleX * 0.5, scaleY * 0.5);
				}, gameScene.widthProperty(), gameScene.heightProperty());

				scalingGroup.scaleXProperty().bind(scaleBinding);
				scalingGroup.scaleYProperty().bind(scaleBinding);
			});
		}

		pauseOverlay.setVisible(true);
		gameScene.setCursor(Cursor.DEFAULT);
	}

	public void hidePauseScreen() {
		if (pauseOverlay != null) {
			pauseOverlay.setVisible(false);
		}
		gameScene.setCursor(Cursor.NONE);
	}
}
