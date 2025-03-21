package dk.sdu.sem;

import dk.sdu.sem.commonsystem.IComponent;

public class HealthComponent implements IComponent {
	private int health;

	public HealthComponent() {
		health = 1;
	}

	public HealthComponent(int health) {
		this.health = health;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public void addHealth(int health) {
		this.health += health;
	}

	public void subHealth(int health) {
		this.health -= health;
	}
}