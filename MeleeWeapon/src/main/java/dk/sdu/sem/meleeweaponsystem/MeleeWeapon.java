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
	private Vector2D position;
	MeleeAnimationController animationController = new MeleeAnimationController(this);





	/**
 * @param direction  Direction of the attack check.
 * @param activator  The entity which activates the weapon.
 */
	@Override
	public void activateWeapon(Entity activator, Vector2D direction) {


		// TODO Code to play attack animation
		// Could use an arc shape rather than circle shape


//		this.radius =
//			activator.getComponent(WeaponComponent.class).getAttackSize()*direction.normalize().y();
		ServiceLoader<ICollisionSPI> collisionSPIServiceLoader = ServiceLoader.load(ICollisionSPI.class);

		TransformComponent transform = activator.getComponent(TransformComponent.class);

		Vector2D position = transform.getPosition();
		float attacksize =
			activator.getComponent(WeaponComponent.class).getAttackSize();
		Vector2D circlecenter = position.add(direction.scale(attacksize/2));

		if (collisionSPIServiceLoader.stream().findFirst().isPresent() ) {
			ICollisionSPI SPI = collisionSPIServiceLoader.iterator().next();

			List<Entity> overlappedEntities = SPI.overlapCircle(
				// the circle with center is slightly offsat such that the
				circlecenter,
				attacksize,
				PhysicsLayer.ENEMY
				// wrong layer maybe if we want it to collide with bullets in
				// future
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
	}

	@Override
	public String getId() {
		return "melee_sweep";
	}
}
