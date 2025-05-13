package dk.sdu.sem.commonsystem.debug;

import javafx.scene.canvas.GraphicsContext;

/**
 * Interface for modules that want to provide their own collider visualization.
 */
public interface IColliderVisualizer {
	/**
	 * Draw colliders to the given graphics context
	 */
	void drawColliders(GraphicsContext gc);
}