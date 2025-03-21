package dk.sdu.sem.enemy;

import dk.sdu.sem.commonsystem.IComponent;

public class EnemyComponent implements IComponent {

	private float moveSpeed = 1.0f;

	public EnemyComponent() {
		this(1.0f);
	}

	public EnemyComponent(float moveSpeed) {
		this.moveSpeed = moveSpeed;
	}

	public float getMoveSpeed() {
		return moveSpeed;
	}
}
