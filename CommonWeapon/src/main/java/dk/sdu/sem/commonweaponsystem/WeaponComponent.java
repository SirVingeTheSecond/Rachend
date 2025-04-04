package dk.sdu.sem.commonweaponsystem;

import dk.sdu.sem.commonsystem.IComponent;

public class WeaponComponent implements IComponent {

	private IWeaponSPI weapon;
	private int damage;
	private double attackTimer;
	public double lastActivated	 = 0;

	public WeaponComponent(IWeaponSPI weapon, int damage, double attackTimer) {
		this.damage = damage;
		this.attackTimer = attackTimer;
		this.weapon = weapon;
	}


	public IWeaponSPI getWeapon() {
		return weapon;
	}
	public int getDamage() {
		return damage;
	}

	public double getAttackTimer() {
		return attackTimer;
	}

}
