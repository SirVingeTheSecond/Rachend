package dk.sdu.sem.collisionsystem.debug;

import dk.sdu.sem.commonsystem.debug.IRaycastRenderer;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class RaycastDebugRenderer implements IRaycastRenderer {
	private static final Logging LOGGER = Logging.createLogger("RaycastDebugRenderer", LoggingLevel.DEBUG);
	private static int frameCounter = 0;

	@Override
	public void drawRaycasts(GraphicsContext gc) {
		try {
			frameCounter++;
			boolean logDetails = frameCounter % 60 == 0;

			if (logDetails) {
				LOGGER.debug("Drawing raycast visualization (frame " + frameCounter + ")");
			}

			// Draw a legend for raycast colors
			int startY = 80;
			int lineHeight = 20;

			gc.setFill(Color.WHITE);
			gc.fillText("RAYCAST VISUALIZATION LEGEND:", 10, startY);

			gc.setFill(Color.GREEN);
			gc.fillText("Green ray = Hit", 20, startY + lineHeight);

			gc.setFill(Color.RED);
			gc.fillText("Red ray = Miss", 20, startY + lineHeight * 2);

			gc.setFill(Color.YELLOW);
			gc.fillText("Yellow circle = Hit point", 20, startY + lineHeight * 3);

			gc.setFill(Color.CYAN);
			gc.fillText("Cyan ray = Surface normal", 20, startY + lineHeight * 4);

			// ToDo: Additional information
			gc.setFill(Color.LIGHTGRAY);

		} catch (Exception e) {
			LOGGER.error("Error in raycast visualizer: " + e.getMessage());
			e.printStackTrace();
		}
	}
}