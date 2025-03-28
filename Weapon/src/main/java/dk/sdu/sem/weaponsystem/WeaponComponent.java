package dk.sdu.sem.weaponsystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;

public class WeaponComponent implements IComponent {
	private Entity bulletEntity;
	public float timer = 0.0f;
	public float cooldown = 0.5f;


	public WeaponComponent(Entity bulletEntity) {
		this.bulletEntity = bulletEntity;
	}

	/// Try to shoot a bullet
	/// If succesful, the onSuccess callback is called and the weapon is put on cooldown
	public void tryShoot(Runnable onSuccess) {
		if (timer > 0) {
			return;
		}

		timer = cooldown;
		onSuccess.run();
	}
}