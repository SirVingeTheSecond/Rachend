package dk.sdu.sem.weaponsystem;

import dk.sdu.sem.commonsystem.IComponent;

public class BulletComponent implements IComponent {
	private int damage;
	private float speed;

	public BulletComponent() {
		this(1, 1.0f);
	}

	public BulletComponent(int damage, float speed) {
		this.damage = damage;
		this.speed = speed;
	}

	public int getDamage() {
		return damage;
	}

	public float getSpeed() {
		return speed;
	}
}