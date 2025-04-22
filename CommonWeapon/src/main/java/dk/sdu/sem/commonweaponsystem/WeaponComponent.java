package dk.sdu.sem.commonweaponsystem;

import dk.sdu.sem.commonsystem.IComponent;

public class WeaponComponent implements IComponent {

	private IWeaponSPI weapon;
	private int damage;
	private int attackSize;
	private double attackTimer;
	public double lastActivated	 = 0;

	public WeaponComponent(IWeaponSPI weapon, int damage, int attackSize, double attackTimer) {
		this.weapon = weapon;
		this.damage = damage;
		this.attackTimer = attackTimer;
		this.attackSize = attackSize;
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

	public synchronized int getAttackSize() {
		return attackSize;
	}

	public synchronized void setAttackSize(int attackSize) {
		this.attackSize = attackSize;
	}
}
