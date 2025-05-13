package dk.sdu.sem.commonsystem.debug;

import dk.sdu.sem.commonsystem.Vector2D;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Interface for the debug drawing manager.
 */
public interface IDebugDrawManager {
	/**
	 * Draws a ray from start in the given direction
	 */
	void drawRay(Vector2D start, Vector2D direction, Color color, float duration);

	/**
	 * Draws a line between two points
	 */
	void drawLine(Vector2D start, Vector2D end, Color color, float duration);

	/**
	 * Draws a circle with the given center and radius
	 */
	void drawCircle(Vector2D center, float radius, Color color, float duration);

	/**
	 * Sets whether the debug drawing is enabled
	 */
	void setEnabled(boolean enabled);

	/**
	 * Checks if debug drawing is enabled
	 */
	boolean isEnabled();

	/**
	 * Clears all debug drawings
	 */
	void clear();

	/**
	 * Updates debug drawings based on delta time.
	 * This removes expired drawings and updates durations.
	 *
	 * @param deltaTime Time in seconds since the last frame
	 */
	void update(double deltaTime);

	/**
	 * Gets information about all debug rays for rendering.
	 *
	 * @return A list of debug ray information objects
	 */
	List<DebugRayInfo> getRays();

	/**
	 * Gets information about all debug circles for rendering.
	 *
	 * @return A list of debug circle information objects
	 */
	List<DebugCircleInfo> getCircles();

	/**
	 * Debug ray information for rendering
	 */
	interface DebugRayInfo {
		Vector2D getStart();
		Vector2D getEnd();
		Color getColor();
		float getDuration();
	}

	/**
	 * Debug circle information for rendering
	 */
	interface DebugCircleInfo {
		Vector2D getCenter();
		float getRadius();
		Color getColor();
		float getDuration();
	}
}