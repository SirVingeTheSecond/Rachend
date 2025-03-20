package dk.sdu.sem.player;

import dk.sdu.sem.commonsystem.IComponent;

/**
 * For now, this is a component to mark an entity as a player.
 */
public class PlayerComponent implements IComponent {
	private float moveSpeed = 1.0f;

	public PlayerComponent() {
		this(1.0f);
	}

	public PlayerComponent(float moveSpeed) {
		this.moveSpeed = moveSpeed;
	}

	public float getMoveSpeed() {
		return moveSpeed;
	}
}
