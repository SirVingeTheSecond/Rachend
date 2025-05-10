package dk.sdu.sem.boss;

import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.IBulletFactory;
import dk.sdu.sem.commonweapon.IWeaponSPI;
import dk.sdu.sem.commonweapon.WeaponComponent;
import dk.sdu.sem.gamesystem.Time;

import java.util.Optional;
import java.util.ServiceLoader;

public class BossWeapon implements IWeaponSPI {
	private static Optional<IBulletFactory> bulletFactory = ServiceLoader.load(IBulletFactory.class).findFirst();

	private float ringOffset = 0;

	@Override
	public String getId() {
		return "boss_weapon";
	}

	@Override
	public void activateWeapon(Entity activator, Vector2D direction) {
		WeaponComponent weaponComponent = activator.getComponent(WeaponComponent.class);
		if (weaponComponent == null) return;

		double currentTime = Time.getTime();
		if (!weaponComponent.canFire(currentTime)) {
			return;
		}

		// Update last fired time
		weaponComponent.setLastActivatedTime(currentTime);

		//Faster fire-rate the lower the health
		StatsComponent statsComponent = activator.getComponent(StatsComponent.class);
		if (statsComponent != null) {
			float maxHealth = statsComponent.getMaxHealth();
			float currentHealth = statsComponent.getCurrentHealth();

			float ratio = currentHealth / maxHealth;

			statsComponent.setBaseStat(StatType.ATTACK_SPEED, 3 - ratio * 2);
		}

		// Get shooter's position
		Vector2D shooterPos = activator.getComponent(TransformComponent.class).getPosition();
		if (shooterPos == null) return;

		if (bulletFactory.isEmpty())
			return;

		IBulletFactory factory = bulletFactory.get();
		float dirChange = (float) (2*Math.PI / 16f);
		for (int i = 0; i < 16; i ++) {
			Vector2D dir = new Vector2D(
				(float) Math.cos(dirChange * i + ringOffset),
				(float) Math.sin(dirChange * i + ringOffset)
			);

			Entity bullet = factory.createBullet(shooterPos, dir, weaponComponent, activator);
			activator.getScene().addEntity(bullet);
		}
		ringOffset += 0.5f;
	}

	@Override
	public float getDamage() {
		return 1;
	}

	@Override
	public float getBulletSpeed() {
		return 150;
	}

	@Override
	public float getAttackSpeed() {
		return 1;
	}

	@Override
	public float getBulletScale() {
		return 1;
	}

	@Override
	public float getBulletKnockback() {
		return 20;
	}
}
