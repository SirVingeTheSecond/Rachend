package dk.sdu.sem.bulletweapon;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.IWeaponSPI;
import dk.sdu.sem.commonweapon.WeaponComponent;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.scenes.SceneManager;

public class BulletWeapon implements IWeaponSPI {
	private static final float BULLET_OFFSET = 20.0f;
	private static final boolean DEBUG = false;

	private final CombatFactory combatFactory = new CombatFactory();

	@Override
	public String getId() {
		return "bullet_weapon";
	}

	@Override
	public void activateWeapon(Entity activator, Vector2D direction) {
		if (activator == null || direction == null) return;

		WeaponComponent weaponComponent = activator.getComponent(WeaponComponent.class);
		if (weaponComponent == null || !weaponComponent.canFire(Time.getTime())) {
			return;
		}

		// Update last fired time
		weaponComponent.setLastActivatedTime(Time.getTime());

		// Get shooter's position
		TransformComponent transform = activator.getComponent(TransformComponent.class);
		if (transform == null) return;
		Vector2D shooterPos = transform.getPosition();

		// Calculate spawn position
		Vector2D normalizedDirection = direction.normalize();
		Vector2D spawnPosition = shooterPos.add(normalizedDirection.scale(BULLET_OFFSET));

		// Create projectile
		Entity projectile = combatFactory.createBullet(
			spawnPosition,
			normalizedDirection,
			weaponComponent.getDamage(),
			activator
		);

		if (projectile != null) {
			// Add projectile to scene if creation was successful
			SceneManager.getInstance().getActiveScene().addEntity(projectile);

			if (DEBUG) {
				System.out.printf("Bullet fired by %s with damage %.1f%n",
					activator.getID(), weaponComponent.getDamage());
			}
		}
	}
}