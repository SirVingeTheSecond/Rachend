package dk.sdu.sem.collisionsystem.debug;

import dk.sdu.sem.commonsystem.debug.IRaycastRenderer;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class RaycastDebugRenderer implements IRaycastRenderer {
	private static final Logging LOGGER = Logging.createLogger("RaycastDebugRenderer", LoggingLevel.DEBUG);

	@Override
	public void drawRaycasts(GraphicsContext gc) {
		try {
			LOGGER.debug("Drawing raycast visualization");

			// Draw a simple indicator that raycast visualization is active
			gc.setFill(Color.WHITE);
			gc.fillText("Raycast Visualization Active", 10, 20);

			// Additional guidance text
			gc.setFill(Color.LIGHTGRAY);
			gc.fillText("Raycasts will appear when game code uses them", 10, 40);

		} catch (Exception e) {
			LOGGER.error("Error in raycast visualizer: " + e.getMessage());
			e.printStackTrace();
		}
	}
}