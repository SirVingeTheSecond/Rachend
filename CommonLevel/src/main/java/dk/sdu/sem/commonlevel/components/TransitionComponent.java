package dk.sdu.sem.commonlevel.components;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Represents an entity that can transition between rooms/scenes
 */
public class TransitionComponent implements IComponent {
	private Vector2D startPosition;
	private Vector2D targetPosition;
	private float transitionProgress = 0.0f;
	private boolean isTransitioning = false;

	public boolean isTransitioning() { return isTransitioning; }

	public void setTransitioning(boolean transitioning) { isTransitioning = transitioning; }

	public Vector2D getStartPosition() { return startPosition; }

	public void setStartPosition(Vector2D startPosition) { this.startPosition = startPosition; }

	public Vector2D getTargetPosition() { return targetPosition; }

	public void setTargetPosition(Vector2D targetPosition) { this.targetPosition = targetPosition; }

	public float getTransitionProgress() { return transitionProgress; }

	public void setTransitionProgress(float progress) { this.transitionProgress = Math.min(1.0f, Math.max(0.0f, progress)); }
}