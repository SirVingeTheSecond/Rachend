package dk.sdu.sem.enemy;

import dk.sdu.sem.commonsystem.IComponent;

/**
 * Component that defines enemy properties.
 */
public class EnemyComponent implements IComponent {
	private float preferredDistance = 20.0f; // Minimum distance to maintain from target

	/**
	 * Creates an enemy component with default properties.
	 */
	public EnemyComponent() {
		this(1.0f);
	}

	/**
	 * Creates an enemy component with specified move speed and preferred distance.
	 *
	 * @param preferredDistance The preferred minimum distance to maintain from target
	 */
	public EnemyComponent(float preferredDistance) {
		this.preferredDistance = preferredDistance;
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