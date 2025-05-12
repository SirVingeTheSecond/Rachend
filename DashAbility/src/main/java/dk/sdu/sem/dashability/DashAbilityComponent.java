package dk.sdu.sem.dashability;

import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.commonsystem.IComponent;

public class DashAbilityComponent implements IComponent {
	private final double dashCooldown = 0.8f;
	private double dashTimer = 0f;

	private PhysicsLayer originalLayer = null;
	private boolean invincibilityActive = false;

	private double fadeDelay = 0.3;
	private double fadeDuration = 0.5;

	private double fadeDelayTimer = 0f;
	private double fadeTimer = 0f;

	private boolean isDelaying = false;
	private boolean isFading = false;

	public float velocityScale = 1000;

	public void update(double deltaTime) {
		if (dashTimer > 0) {
			dashTimer = Math.max(dashTimer - deltaTime, 0);

			// If cooldown just completed, start the delay timer
			if (dashTimer == 0 && !isDelaying && !isFading) {
				isDelaying = true;
				fadeDelayTimer = fadeDelay;
			}
		}

		// Update delay timer if we're in delay phase
		if (isDelaying) {
			fadeDelayTimer = Math.max(fadeDelayTimer - deltaTime, 0);
			if (fadeDelayTimer == 0) {
				isDelaying = false;
				isFading = true;
				fadeTimer = fadeDuration;
			}
		}

		// Update fade timer if we're fading
		if (isFading) {
			fadeTimer = Math.max(fadeTimer - deltaTime, 0);
			if (fadeTimer == 0) {
				isFading = false;
			}
		}
	}

	public boolean isOnCooldown() {
		return dashTimer > 0;
	}

	public void use() {
		dashTimer = dashCooldown;
		isDelaying = false;
		isFading = false;
	}

	public double progress() {
		return 1 - dashTimer / dashCooldown;
	}

	public boolean isBarVisible() {
		return isOnCooldown() || isDelaying || isFading;
	}

	public double getFadeOpacity() {
		if (isOnCooldown() || isDelaying) return 1.0; // Full opacity during cooldown or delay
		return fadeTimer / fadeDuration;
	}

	// Getter/setter for fade delay
	public double getFadeDelay() {
		return fadeDelay;
	}

	public void setFadeDelay(double delay) {
		this.fadeDelay = Math.max(0, delay);
	}

	public double getFadeDuration() {
		return fadeDuration;
	}

	public void setFadeDuration(double duration) {
		this.fadeDuration = Math.max(0.1, duration);
	}

	public boolean isActivelyDashing() {
		return dashTimer > 0;
	}

	public void setOriginalLayer(PhysicsLayer layer) {
		this.originalLayer = layer;
	}

	public PhysicsLayer getOriginalLayer() {
		return originalLayer;
	}

	public boolean isInvincibilityActive() {
		return invincibilityActive;
	}

	public void setInvincibilityActive(boolean active) {
		this.invincibilityActive = active;
	}
}