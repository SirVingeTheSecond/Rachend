package dk.sdu.sem.player;

import dk.sdu.sem.commonsystem.IComponent;

/**
 * Component that marks an entity as a player.
 */
public class PlayerComponent implements IComponent {
	private float moveSpeed;
	private boolean inputEnabled;

	/**
	 * Creates a player component with default values.
	 */
	public PlayerComponent() {
		this(4000.0f);
	}

	/**
	 * Creates a player component with specified move speed.
	 *
	 * @param moveSpeed The movement speed of the player
	 */
	public PlayerComponent(float moveSpeed) {
		this.moveSpeed = moveSpeed;
		this.inputEnabled = true; // Input enabled by default
	}

	/**
	 * Gets the player's movement speed.
	 *
	 * @return The movement speed
	 */
	public float getMoveSpeed() {
		return moveSpeed;
	}

	/**
	 * Sets the player's movement speed.
	 *
	 * @param moveSpeed The new movement speed
	 */
	public void setMoveSpeed(float moveSpeed) {
		this.moveSpeed = moveSpeed;
	}

	/**
	 * Checks if player input is currently enabled.
	 *
	 * @return true if input is enabled, false otherwise
	 */
	public boolean isInputEnabled() {
		return inputEnabled;
	}

	/**
	 * Enables or disables player input.
	 *
	 * @param enabled true to enable input, false to disable
	 */
	public void setInputEnabled(boolean enabled) {
		this.inputEnabled = enabled;
	}
}