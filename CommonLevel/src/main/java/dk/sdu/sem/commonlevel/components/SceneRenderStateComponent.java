package dk.sdu.sem.commonlevel.components;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import javafx.scene.image.WritableImage;

/**
 * Component that tracks scene rendering states for transitions
 */
public class SceneRenderStateComponent implements IComponent {
	private WritableImage sceneSnapshot;
	private Vector2D position = Vector2D.ZERO;
	private boolean isActive = false;

	public WritableImage getSceneSnapshot() { return sceneSnapshot; }

	public void setSceneSnapshot(WritableImage snapshot) { this.sceneSnapshot = snapshot; }

	public Vector2D getPosition() { return position; }

	public void setPosition(Vector2D position) { this.position = position; }

	public boolean isActive() { return isActive; }

	public void setActive(boolean active) { isActive = active; }
}