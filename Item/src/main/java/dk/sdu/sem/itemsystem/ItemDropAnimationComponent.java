package dk.sdu.sem.itemsystem;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Component for item drop animation.
 */
public class ItemDropAnimationComponent implements IComponent {
	private static final float BOUNCE_FACTOR = 0.6f; // Energy retention after bounce
	private static final int MAX_BOUNCES = 2; // Maximum number of bounces before settling

	private Vector2D initialVelocity;
	private float groundLevel; // The Y-coordinate where the item should stop
	private int bounceCount = 0;
	private boolean isAnimating = true;
	private float timeAlive = 0f;

	public ItemDropAnimationComponent(Vector2D initialVelocity, float groundLevel) {
		this.initialVelocity = initialVelocity;
		this.groundLevel = groundLevel;
	}

	public Vector2D getInitialVelocity() {
		return initialVelocity;
	}

	public float getGroundLevel() {
		return groundLevel;
	}

	public boolean isAnimating() {
		return isAnimating;
	}

	public void setAnimating(boolean animating) {
		this.isAnimating = animating;
	}

	public int getBounceCount() {
		return bounceCount;
	}

	public void incrementBounceCount() {
		bounceCount++;
		if (bounceCount >= MAX_BOUNCES) {
			isAnimating = false;
		}
	}

	public boolean canBounce() {
		return bounceCount < MAX_BOUNCES;
	}

	public float getBounceFactor() {
		return BOUNCE_FACTOR;
	}

	public float getTimeAlive() {
		return timeAlive;
	}

	public void updateTimeAlive(float deltaTime) {
		this.timeAlive += deltaTime;
	}
}