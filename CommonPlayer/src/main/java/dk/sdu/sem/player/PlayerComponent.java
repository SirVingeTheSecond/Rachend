package dk.sdu.sem.player;

import dk.sdu.sem.commonsystem.IComponent;

/**
 * For now, this is a component to mark an entity as a player.
 */
public class PlayerComponent implements IComponent {
	private float moveSpeed;
	
	public PlayerState state = PlayerState.IDLE;

	public PlayerComponent() {
		this(4000.0f);
	}

	public PlayerComponent(float moveSpeed) {
		this.moveSpeed = moveSpeed;
	}

	public float getMoveSpeed() {
		return moveSpeed;
	}
}
