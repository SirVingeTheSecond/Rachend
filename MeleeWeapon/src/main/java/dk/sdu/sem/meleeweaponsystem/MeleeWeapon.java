package dk.sdu.sem.meleeweaponsystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.IMeleeWeapon;
import dk.sdu.sem.commonweapon.WeaponComponent;
import dk.sdu.sem.enemy.EnemyComponent;

import java.util.List;
import java.util.ServiceLoader;

public class MeleeWeapon implements IMeleeWeapon {
	private final ICollisionSPI collisionService;
	private final MeleeAnimationController animationController = new MeleeAnimationController(this);
	private Vector2D position;

	public MeleeWeapon () {
		collisionService = ServiceLoader.load(ICollisionSPI.class).findFirst().orElse(null);
	}

	/**
	* @param direction Direction of the attack check.
	* @param activator The entity which activates the weapon.
	*/
	@Override
	public void activateWeapon(Entity activator, Vector2D direction) {
		if (collisionService == null) {
			System.out.println("Collision service not available");
			return;
		}

		// TODO Code to play attack animation
		// Could use an arc shape rather than circle shape
		//this.radius = activator.getComponent(WeaponComponent.class).getAttackSize()*direction.normalize().y();

		TransformComponent transform = activator.getComponent(TransformComponent.class);
		Vector2D position = transform.getPosition();
		float attackSize = activator.getComponent(WeaponComponent.class).getAttackSize();
		Vector2D circleCenter = position.add(direction.scale(attackSize/2));

		List<Entity> overlappedEntities = collisionService.overlapCircle(
				circleCenter,
				attackSize,
				PhysicsLayer.ENEMY
		);

		for (Entity entity : overlappedEntities) {
			System.out.print("Count of overlappedEntities: " + overlappedEntities.size());
			if (entity.hasComponent(EnemyComponent.class)) {
				System.out.println("Player overlapped an Enemy: " + entity.getID());
				if (entity.hasComponent(StatsComponent.class)) {
					System.out.println("Enemy has stats");
					// rounding might cause issues
					float currentHealth = entity.getComponent(StatsComponent.class).getCurrentHealth();
					entity.getComponent(StatsComponent.class).setCurrentHealth(currentHealth - 1);
				}
			}
		}
	}

	@Override
	public String getId() {
		return "melee_sweep";
	}
}
