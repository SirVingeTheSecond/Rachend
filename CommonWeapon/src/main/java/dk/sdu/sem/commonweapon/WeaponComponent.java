package dk.sdu.sem.commonweapon;

import dk.sdu.sem.commonsystem.IComponent;

public class WeaponComponent implements IComponent {

	IWeapon weapon;
	private int damage;
	private float attackRate;

//	public WeaponComponent() {
//		this(1, 1.0f);
//	}

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