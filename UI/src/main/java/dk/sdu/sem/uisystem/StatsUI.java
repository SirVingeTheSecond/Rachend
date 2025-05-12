package dk.sdu.sem.uisystem;

import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Pair;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.Map;

public class StatsUI implements IGUIUpdate {
	private Map<StatType, Float> lastStats = new HashMap<>();

	private Map<StatType, Pair<Double, Float>> statChangeTimestamps = new HashMap<>();

	private float highlightDuration = 3;

	private final float textAlpha = 0.3f;

	@Override
	public void onGUI(GraphicsContext gc) {
		HealthBarNode node = NodeManager.active().getNodes(HealthBarNode.class).stream().findFirst().orElse(null);
		if (node == null)
			return;

		int x = 5;
		int y = 80;

		//Use this font to support formatting
		gc.setFont(Font.font("Courier New", FontWeight.BOLD, 14));

		for (var stat : node.stats.getAllStats().entrySet()) {
			float value = stat.getValue();
			Float oldValue = lastStats.get(stat.getKey());
			String label;

			//Only checking relevant stats, and shortening names
			switch (stat.getKey()) {
				case DAMAGE -> label = "DMG:";
				case MOVE_SPEED -> label = "SPD:";
				case ATTACK_SPEED -> label = "ATK SPD:";
				case ARMOR -> label = "ARM:";
				case BULLET_SCALE -> label = "B. SCALE:";
				case BULLET_KNOCKBACK -> label = "KB:";
				case BULLET_SPEED -> label = "B. SPD:";
				default -> { continue; }
			}

			//Remember when stat changes and what it was before
			if (oldValue != null && value != oldValue) {
				statChangeTimestamps.put(stat.getKey(), new Pair<>(Time.getTime(), oldValue));
			}

			String text = String.format("%-9s %8.2f", label, value);
			Color mainColor = Color.rgb(255, 255, 255, textAlpha);

			//Change the color and show difference for a duration
			Pair<Double, Float> changedAt = statChangeTimestamps.get(stat.getKey());
			if (changedAt != null && Time.getTime() - changedAt.getFirst() < highlightDuration) {
				if (value > changedAt.getSecond()) {
					mainColor = Color.rgb(0, 255, 0, textAlpha);
					text += String.format("(+%.2f)", value - changedAt.getSecond());
				} else if (value < changedAt.getSecond()) {
					mainColor = Color.rgb(255, 0, 0, textAlpha);
					text += String.format("(%.2f)", value - changedAt.getSecond());
				}
			}

			// Manual text outline (easier to read)
			gc.setFill(Color.rgb(0,0,0,textAlpha));
			gc.setFontSmoothingType(null);
			gc.fillText(text, x - 2, y);
			gc.fillText(text, x + 2, y);
			gc.fillText(text, x, y - 2);
			gc.fillText(text, x, y + 2);

			// Main colored text
			gc.setFill(mainColor);
			gc.fillText(text, x, y);

			// Move to next line
			y += 15;

			lastStats.put(stat.getKey(), value);
		}
	}
}
