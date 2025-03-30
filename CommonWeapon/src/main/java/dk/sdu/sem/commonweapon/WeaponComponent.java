package dk.sdu.sem.weaponsystem;

import dk.sdu.sem.TestNode;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.

public class WeaponComponent implements IComponent {
	IWeapon weapon;
	private int damage;
	private float attackRate;

	public WeaponComponent() {
		this(1, 1.0f);
	}

	public WeaponComponent(int damage, float speed) {
		this.damage = damage;
		this.attackRate = speed;
	}

	public int getDamage() {
		return damage;
	}

	public float getAttackRate() {
		return attackRate;
	}
}