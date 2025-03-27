package dk.sdu.sem.weaponsystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.gamesystem.Time;

public class WeaponComponent implements IComponent {
	private Entity bulletEntity;
	public float timer = 0.0f;
	public float cooldown = 0.5f;


	public WeaponComponent(Entity bulletEntity) {
		this.bulletEntity = bulletEntity;
	}

	public Entity getBulletEntity() {
		return bulletEntity;
	}

	public void tryShoot(Runnable onSuccess) {
		if (timer > 0) {
			return;
		}

		timer = cooldown;
		onSuccess.run();
	}
}