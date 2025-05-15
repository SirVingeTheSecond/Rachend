package dk.sdu.sem.commonsystem.debug;

import javafx.scene.canvas.GraphicsContext;

/**
 * Interface for visualizing raycasts.
 */
public interface IRaycastRenderer {
	/**
	 * Draw raycast visualization using the graphics context
	 */
	void drawRaycasts(GraphicsContext gc);
}