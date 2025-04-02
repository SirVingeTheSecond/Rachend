package dk.sdu.sem.commonweaponsystem;

import dk.sdu.sem.commonsystem.IComponent;

public class WeaponComponent implements IComponent {

	private IWeapon weapon;
	private int damage;
	private float attackRate;

	public WeaponComponent(IWeapon weapon, int damage, float attackRate) {
		this.damage = damage;
		this.attackRate = attackRate;
		this.weapon = weapon;
	}


	public IWeapon getWeapon() {
		return weapon;
	}
	public int getDamage() {
		return damage;
	}

	public float getAttackRate() {
		return attackRate;
	}

}
