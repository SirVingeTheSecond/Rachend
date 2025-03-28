package dk.sdu.sem.commonhealth;

import dk.sdu.sem.commonsystem.IComponent;

public class HealthComponent implements IComponent {
	private int health;
	private int maxHealth;

	public HealthComponent() {}

	public HealthComponent(int health, int maxHealth) {
		this.health = health;
		this.maxHealth = maxHealth;
	}

	public HealthComponent(int health) {
		this.health = health;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = Math.min(maxHealth, health);
	}

	public void addHealth(int health) {
		this.health = Math.min(maxHealth, this.health + health);
	}

	public void subHealth(int health) {
		this.health = Math.min(maxHealth, this.health - health);;
	}

	public int getMaxHealth() {
		return maxHealth;
	}

	public void setMaxHealth(int maxHealth) {
		this.maxHealth = maxHealth;
		this.health = Math.min(maxHealth, this.health);
	}
}