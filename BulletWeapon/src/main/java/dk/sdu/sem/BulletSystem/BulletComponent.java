package dk.sdu.sem.BulletSystem;

import dk.sdu.sem.commonsystem.IComponent;

public class BulletComponent implements IComponent {
	private int speed;

	public BulletComponent() {
		this(200);
	}

	public BulletComponent(int speed) {
		this.speed = speed;
	}

	public int getSpeed() {
		return speed;
	}
}
