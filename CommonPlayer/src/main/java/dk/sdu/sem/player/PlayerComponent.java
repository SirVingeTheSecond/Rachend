package dk.sdu.sem.player;

import dk.sdu.sem.commonsystem.IComponent;

/**
 * Component that marks an entity as a player.
 */
public class PlayerComponent implements IComponent {
	private boolean inputEnabled;

	/**
	 * Creates a player component
	 */
	public PlayerComponent() {
		this.inputEnabled = true; // Input enabled by default
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