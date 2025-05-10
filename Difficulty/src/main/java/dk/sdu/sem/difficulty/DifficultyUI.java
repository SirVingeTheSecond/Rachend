package dk.sdu.sem.difficulty;

import dk.sdu.sem.commonsystem.Difficulty;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class DifficultyUI implements IGUIUpdate {
	static boolean difficultyIncreased = false;

	private double increasedTime; //= Double.MAX_VALUE;
	private float xPos = GameConstants.WORLD_SIZE.x() * GameConstants.TILE_SIZE / 2;
	private float yPos = GameConstants.WORLD_SIZE.y() * GameConstants.TILE_SIZE / 2;

	@Override
	public void onGUI(GraphicsContext gc) {

		if (difficultyIncreased) {
			increasedTime = Time.getTime();
			difficultyIncreased = false;
		}

		//Show text for 5 seconds
		if (Time.getTime() - increasedTime < 5) {
			drawText(gc, "FLOOR " + (Difficulty.getLevel()) + " CLEARED!", xPos, yPos, 26, Color.LIMEGREEN);
			drawText(gc, "Continuing in " + Math.ceil(5 - (Time.getTime() - increasedTime)) + " seconds.", xPos, yPos + 50, 14, Color.ORANGE);
		}
	}

	private void drawText(GraphicsContext gc, String text, float x, float y, float size, Color color) {
		gc.setFont(Font.font("Courier New", FontWeight.BOLD, size));

		// Manual text outline (easier to read)
		gc.setFill(Color.rgb(0,0,0, 1));
		gc.setTextAlign(TextAlignment.CENTER);
		gc.setFontSmoothingType(null);
		gc.fillText(text, x - 2, y);
		gc.fillText(text, x + 2, y);
		gc.fillText(text, x, y - 2);
		gc.fillText(text, x, y + 2);

		// Main colored text
		gc.setFill(color);
		gc.fillText(text, x, y);
		gc.setTextAlign(TextAlignment.LEFT);
	}
}
