package dk.sdu.sem.BulletSystem;

import dk.sdu.sem.commonsystem.IComponent;

public class BulletComponent implements IComponent {
	public IComponent transformComponent;
	private int speed;

	public int getSpeed() {
		return speed;
	}
}
