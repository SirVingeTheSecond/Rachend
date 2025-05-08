package dk.sdu.sem.hitmarkers;

import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Game;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.*;
import java.util.function.BiConsumer;

public class HitMarkerSystem {
	private static HashMap<StatsComponent, BiConsumer<Float, Float>> registeredStats = new HashMap<>();

	public static void registerNode(HitMarkerNode hitMarkerNode) {
		if (registeredStats.containsKey(hitMarkerNode.stats))
			return;

		BiConsumer<Float, Float> consumer = (oldValue, newValue) -> healthChanged(hitMarkerNode, oldValue, newValue);

		hitMarkerNode.stats.addStatChangeListener(StatType.CURRENT_HEALTH, consumer);
		registeredStats.put(hitMarkerNode.stats, consumer);
	}

	public static void unregisterNode(HitMarkerNode hitMarkerNode) {
		hitMarkerNode.stats.removeStatChangeListener(StatType.CURRENT_HEALTH, registeredStats.get(hitMarkerNode.stats));
		registeredStats.remove(hitMarkerNode.stats);
	}

	private static void healthChanged(HitMarkerNode node, float oldValue, float newValue) {
		if (newValue > oldValue)
			return;

		Vector2D position = node.transform.getPosition();
		Point2D scenePos = Game.getInstance().getCanvas().localToScene(position.x(), position.y());

		Text text = new Text(Math.round(oldValue - newValue) + "");
		text.setManaged(false);
		text.setStyle(
			"""
			-fx-font-size: 20px;
			-fx-stroke: black;
			-fx-stroke-width: 1;
			-fx-fill: #ffffff;
			-fx-font-weight: bold;
			-fx-font-family: Impact;
			"""
		);
		float scale = (float) Game.getInstance().getCanvas().getScaleX();

		text.setLayoutX(scenePos.getX() + (Math.random() * 40 - 20) * scale);
		text.setLayoutY(scenePos.getY() - 25 * scale);
		text.setScaleX(0);
		text.setScaleY(0);

		KeyValue scaleAnimX = new KeyValue(text.scaleXProperty(), scale, Interpolator.EASE_OUT);
		KeyValue scaleAnimY = new KeyValue(text.scaleYProperty(), scale, Interpolator.EASE_OUT);

		KeyFrame frame1 = new KeyFrame(Duration.seconds(0.1), scaleAnimX, scaleAnimY);

		KeyValue scaleAnimX2 = new KeyValue(text.scaleXProperty(), 0, Interpolator.EASE_IN);
		KeyValue scaleAnimY2 = new KeyValue(text.scaleYProperty(), 0, Interpolator.EASE_IN);

		KeyFrame frame2 = new KeyFrame(Duration.seconds(0.3), scaleAnimX2, scaleAnimY2);

		Timeline timeline = new Timeline(frame1, frame2);
		timeline.setOnFinished(event -> {
			Game.getInstance().getRoot().getChildren().remove(text);
		});

		Game.getInstance().getRoot().getChildren().add(text);

		timeline.play();
	}
}
