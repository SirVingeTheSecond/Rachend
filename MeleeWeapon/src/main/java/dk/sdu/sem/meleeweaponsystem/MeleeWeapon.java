package dk.sdu.sem.meleeweaponsystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.IMeleeWeaponSPI;
import dk.sdu.sem.commonweapon.WeaponComponent;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

public class MeleeWeapon implements IMeleeWeaponSPI {
	private static final Logging LOGGER = Logging.createLogger("MeleeWeapon", LoggingLevel.DEBUG);

	private final MeleeCombatFactory factory = new MeleeCombatFactory();

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

		// Get activator's position
		TransformComponent transform = activator.getComponent(TransformComponent.class);
		if (transform == null) return;

		Vector2D position = transform.getPosition();

		// Create melee effect using the factory
		Entity meleeEffect = factory.createMeleeEffect(
			position,
			direction.normalize(),
			getAttackScale(),
			activator
		);

		// Add effect to scene
		SceneManager.getInstance().getActiveScene().addEntity(meleeEffect);

		LOGGER.debug("Melee attack activated by %s", activator.getID());
	}

	@Override
	public String getId() {
		return "melee_sweep";
	}

	@Override
	public float getDamage() {
		return 2.0f;
	}

	@Override
	public float getAttackSpeed() {
		return 1.2f;
	}

	// 50 for Enemy?
	// 60 for Player?
	@Override
	public float getAttackScale() {
		return 60.0f;
	}
}