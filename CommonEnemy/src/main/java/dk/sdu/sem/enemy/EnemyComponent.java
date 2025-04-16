package dk.sdu.sem.enemy;

import dk.sdu.sem.commonsystem.IComponent;

/**
 * Component that defines enemy properties.
 */
public class EnemyComponent implements IComponent {
	private float moveSpeed = 1.0f;
	private float preferredDistance = 20.0f; // Minimum distance to maintain from target

	/**
	 * Creates an enemy component with default properties.
	 */
	public EnemyComponent() {
		this(1.0f, 20.0f);
	}

	/**
	 * Creates an enemy component with specified move speed.
	 *
	 * @param moveSpeed The movement speed
	 */
	public EnemyComponent(float moveSpeed) {
		this(moveSpeed, 20.0f);
	}

	/**
	 * Creates an enemy component with specified move speed and preferred distance.
	 *
	 * @param moveSpeed The movement speed
	 * @param preferredDistance The preferred minimum distance to maintain from target
	 */
	public EnemyComponent(float moveSpeed, float preferredDistance) {
		this.moveSpeed = moveSpeed;
		this.preferredDistance = preferredDistance;
	}

	/**
	 * Gets the enemy's movement speed.
	 *
	 * @return The movement speed
	 */
	public float getMoveSpeed() {
		return moveSpeed;
	}

	/**
	 * Sets the enemy's movement speed.
	 *
	 * @param moveSpeed The new movement speed
	 */
	public void setMoveSpeed(float moveSpeed) {
		this.moveSpeed = moveSpeed;
	}

	/**
	 * Gets the preferred minimum distance to maintain from target.
	 *
	 * @return The preferred distance
	 */
	public float getPreferredDistance() {
		return preferredDistance;
	}

	/**
	 * Sets the preferred minimum distance to maintain from target.
	 *
	 * @param preferredDistance The new preferred distance
	 */
	public void setPreferredDistance(float preferredDistance) {
		this.preferredDistance = preferredDistance;
	}
}