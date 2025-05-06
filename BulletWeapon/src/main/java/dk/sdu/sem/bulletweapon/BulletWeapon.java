package dk.sdu.sem.bulletweapon;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.IRangedWeaponSPI;
import dk.sdu.sem.commonweapon.WeaponComponent;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

public class BulletWeapon implements IRangedWeaponSPI {
	private static final Logging LOGGER = Logging.createLogger("BulletWeapon", LoggingLevel.DEBUG);
	private static final float BULLET_OFFSET = 20.0f;

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

		// Calculate spawn position with offset
		Vector2D normalizedDirection = direction.normalize();
		Vector2D spawnPosition = shooterPos.add(normalizedDirection.scale(BULLET_OFFSET));

		// Create projectile using the combat factory
		Entity projectile = combatFactory.createBullet(
			spawnPosition,
			normalizedDirection,
			getDamage(),
			getBulletSpeed(),
			getAttackScale(),
			activator
		);

		// Add projectile to scene
		SceneManager.getInstance().getActiveScene().addEntity(projectile);

		LOGGER.debug("Bullet fired by %s with damage %.1f", activator.getID(), getDamage());
	}

	@Override
	public float getDamage() {
		return 1.0f;
	}

	@Override
	public float getBulletSpeed() {
		return 150.0f;
	}

	@Override
	public float getAttackSpeed() {
		return 2.0f;
	}

	@Override
	public float getAttackScale() {
		return 1.2f;
	}
}