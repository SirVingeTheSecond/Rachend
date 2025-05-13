package dk.sdu.sem.itemsystem;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Component for item drop animation.
 */
public class ItemDropAnimationComponent implements IComponent {
	// Energy retention after each bounce - higher values = bouncier items (range 0.0-1.0)
	private static final float BOUNCE_FACTOR = 0.5f;

	// Maximum number of bounces before item settles (includes initial bounce)
	private static final int MAX_BOUNCES = 2;

	private final Vector2D initialVelocity;
	private final float groundLevel; // The y-coordinate where the item should stop

	private Vector2D restingPosition;
	private float timeAlive = 0f;
	private int bounceCount = 0;
	private boolean isAnimating = true;
	private boolean readyToSettle = false;

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

	public Vector2D getRestingPosition() {
		return restingPosition;
	}

	public void setRestingPosition(Vector2D position) {
		this.restingPosition = position;
	}

	public boolean isAnimating() {
		return isAnimating;
	}

	public void setAnimating(boolean animating) {
		this.isAnimating = animating;
	}

	public boolean isReadyToSettle() {
		return readyToSettle;
	}

	public void setReadyToSettle(boolean readyToSettle) {
		this.readyToSettle = readyToSettle;
	}

	public int getBounceCount() {
		return bounceCount;
	}

	public void incrementBounceCount() {
		bounceCount++;
		if (bounceCount >= MAX_BOUNCES) {
			readyToSettle = true;
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