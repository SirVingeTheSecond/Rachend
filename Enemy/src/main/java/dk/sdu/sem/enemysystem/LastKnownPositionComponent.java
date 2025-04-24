package dk.sdu.sem.enemysystem;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Tracks the player's last known location and the current AI state.
 */
public class LastKnownPositionComponent implements IComponent {
	private Vector2D lastKnownPosition = new Vector2D(0, 0);
	private EnemyState state = EnemyState.IDLE;

	public Vector2D getLastKnownPosition() {
		return lastKnownPosition;
	}

	public void setLastKnownPosition(Vector2D pos) {
		this.lastKnownPosition = pos;
	}

	public EnemyState getState() {
		return state;
	}

	public void setState(EnemyState state) {
		this.state = state;
	}
}