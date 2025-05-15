package dk.sdu.sem.commonsystem.debug;

import dk.sdu.sem.commonsystem.Vector2D;
import javafx.scene.paint.Color;

/**
 * Interface for the debug drawing manager that handles primitive drawing operations.
 */
public interface IDebugDrawManager {
	/** Draws a ray from start in the given direction */
	void drawRay(Vector2D start, Vector2D direction, Color color, float duration);

	/** Draws a line between two points */
	void drawLine(Vector2D start, Vector2D end, Color color, float duration);

	/** Draws a circle */
	void drawCircle(Vector2D center, float radius, Color color, float duration);

	/** Draws a rectangle */
	void drawRect(Vector2D position, float width, float height, Color color, float duration);

	/** Draws text at the specified position */
	void drawText(String text, Vector2D position, Color color, float duration);

	/** Updates debug drawings, removing expired elements */
	void update(double deltaTime);

	/** Checks if debug drawing is enabled */
	boolean isEnabled();

	/** Sets whether debug drawing is enabled */
	void setEnabled(boolean enabled);

	/** Clears all debug drawings */
	void clear();
}