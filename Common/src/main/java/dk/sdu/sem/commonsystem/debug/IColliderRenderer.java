package dk.sdu.sem.commonsystem.debug;

import javafx.scene.canvas.GraphicsContext;

/**
 * Interface for visualizing colliders.
 */
public interface IColliderRenderer {
	/**
	 * Draw collider visualization using the graphics context
	 */
	void drawColliders(GraphicsContext gc);
}