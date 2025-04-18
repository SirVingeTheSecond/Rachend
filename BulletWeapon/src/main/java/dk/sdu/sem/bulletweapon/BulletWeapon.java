package dk.sdu.sem.bulletweapon;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.IWeaponSPI;
import dk.sdu.sem.commonweapon.WeaponComponent;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.scenes.SceneManager;

/**
 * Implementation of IWeaponSPI that fires bullets.
 */
public class BulletWeapon implements IWeaponSPI {
	private static final float BULLET_OFFSET = 20.0f; // Spawn distance from shooter
	private static final boolean DEBUG = false;

	private final CombatFactory combatFactory = new CombatFactory();

	@Override
	public String getId() {
		return "bullet_weapon";
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

		// Get shooter's position
		Vector2D shooterPos = activator.getComponent(TransformComponent.class).getPosition();
		if (shooterPos == null) return;

		// Calculate spawn position (offset from shooter in the direction of fire)
		Vector2D normalizedDirection = direction.normalize();
		Vector2D spawnPosition = shooterPos.add(normalizedDirection.scale(BULLET_OFFSET));

		// Create projectile using the combat factory
		Entity projectile = combatFactory.createBullet(
			spawnPosition,
			normalizedDirection,
			weaponComponent.getDamage(),
			activator
		);

		// Add projectile to scene
		SceneManager.getInstance().getActiveScene().addEntity(projectile);

		if (DEBUG) {
			System.out.printf("Bullet fired by %s with damage %.1f%n",
				activator.getID(), weaponComponent.getDamage());
		}
	}
}