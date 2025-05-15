package dk.sdu.sem.meleeweaponsystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Timer;

/**
 * Component for melee attack effects.
 */
public class MeleeEffectComponent implements IComponent {
	private final Timer lifetimeTimer;
	private final Entity owner;
	private final float attackRange;
	private boolean hasDealtDamage;
	private boolean animationChangedToStrike;
	private final float lifetime;
	private float elapsedTime = 0f;

	public MeleeEffectComponent(float lifetime, Entity owner, float attackRange) {
		this.lifetime = lifetime;
		this.lifetimeTimer = new Timer(lifetime);
		this.owner = owner;
		this.attackRange = attackRange;
		this.hasDealtDamage = false;
		this.animationChangedToStrike = false;
	}

	/**
	 * Updates the component timer.
	 * Returns true if the effect has completed its lifetime.
	 */
	public boolean update(double deltaTime) {
		elapsedTime += (float) deltaTime;
		return lifetimeTimer.update(deltaTime);
	}

	public float getLifetime() {
		return lifetime;
	}

	public float getElapsedTime() {
		return elapsedTime;
	}

	public Entity getOwner() {
		return owner;
	}

	public float getAttackRange() {
		return attackRange;
	}

	public boolean hasDealtDamage() {
		return hasDealtDamage;
	}

	public void setHasDealtDamage(boolean hasDealtDamage) {
		this.hasDealtDamage = hasDealtDamage;
	}

	public boolean hasChangedToStrikeAnimation() {
		return animationChangedToStrike;
	}

	public void setChangedToStrikeAnimation(boolean changed) {
		this.animationChangedToStrike = changed;
	}

	/**
	 * Checks if the effect has reached the strike phase timing
	 */
	public boolean shouldTriggerStrike() {
		// If we've already triggered it, don't trigger again
		if (animationChangedToStrike) {
			return false;
		}

		// Check if we've reached 30% of the total lifetime
		return elapsedTime > lifetime * 0.3f;
	}

	/**
	 * Checks if the effect has completed its lifetime
	 */
	public boolean isCompleted() {
		return elapsedTime >= lifetime;
	}
}