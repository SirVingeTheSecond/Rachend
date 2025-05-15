package dk.sdu.sem.commonsystem.debug;

import javafx.scene.canvas.GraphicsContext;

/**
 * Interface for visualizing pathfinding.
 */
public interface IPathfindingRenderer {
	/**
	 * Draw pathfinding visualization using the graphics context
	 */
	void drawPaths(GraphicsContext gc);
}