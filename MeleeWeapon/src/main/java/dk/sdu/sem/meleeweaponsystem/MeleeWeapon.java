package dk.sdu.sem.meleeweaponsystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.data.RaycastHit;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.IMeleeWeapon;
import dk.sdu.sem.commonweapon.WeaponComponent;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.player.PlayerComponent;

import java.util.List;
import java.util.ServiceLoader;

public class MeleeWeapon implements IMeleeWeapon {
	private final ICollisionSPI collisionService;
	private Vector2D position;
	private Vector2D effectPosition;
	private float effectRotation ;

	public MeleeWeapon () {
		collisionService = ServiceLoader.load(ICollisionSPI.class).findFirst().orElse(null);
	}

	/**
	* @param direction Direction of the attack check.
	* @param activator The entity which activates the weapon.
	*/


	@Override
	public void activateWeapon(Entity activator, Vector2D direction) {

		// A weapon is always stored in a weaponcomponent so no need to check
		// if weaponcomponent exists.
		WeaponComponent weaponComponent =
				activator.getComponent(WeaponComponent.class);
		double currentTime = Time.getTime();
		if (!weaponComponent.canFire(currentTime)) {
			return;
		}
		// Update last fired time
		weaponComponent.setLastActivatedTime(currentTime);
		if (collisionService == null) {
			System.out.println("Collision service not available");
			return;
		}

		// setupcode for reuse multiple times
		TransformComponent transform = activator.getComponent(TransformComponent.class);
		Vector2D position = transform.getPosition();
		float attackSize = activator.getComponent(WeaponComponent.class).getAttackSize();
		Vector2D circleCenter = position.add(direction.scale(attackSize));

		// better performance might be achived by only setting
		// transformcomponent location at each weapon activation
		// and changing animation state to sweep, after that turn off animation
		Entity animationEntity = new Entity();
		animationEntity.addComponent(new AnimatorComponent());
		AnimatorComponent animator =
				animationEntity.getComponent(AnimatorComponent.class);
		animator.addState("tryswipe", "beg_partialSwipe");
		animator.addState("swiping", "melee_swipe");

		// Ideally the  two animations would get added to
		// oneshotanimation as a priorirtyqueue assuring that tryswipe is ran
		// before swiping, as they would then return to default animation or
		// do nothing.

		// The animation needs to be on the fringe of the circle hitbox in
		// the direction attacking.
		animationEntity.addComponent(new TransformComponent(position.add(direction.scale(attackSize)),
				direction.angle()));
		animationEntity.addComponent(new SpriteRendererComponent());

		// telegraph to player that the weapon is activated.
		animator.setCurrentState("tryswipe");
		// Could use an arc shape rather than circle shape
		// Step 2 Detect what entity was hit
		List<Entity> overlappedEntities = collisionService.overlapCircle(
				circleCenter,
				attackSize,
				// naive solution that assumes that the activator has a collider
				resolvePhysicsLayer(activator)
		);

		Scene.getActiveScene().addEntity(animationEntity);
		// check if something was hit
		if (!overlappedEntities.isEmpty()) {
			animator.setCurrentState("swiping");

			// remove health for each hit entity
			for (Entity entity : overlappedEntities) {
				// check if we hit a wall before we hit the entity
				Vector2D entityPos =
						entity.getComponent(TransformComponent.class).getPosition();
				RaycastHit raycastHit = collisionService.raycast(
						entityPos,
						entityPos.subtract(position).normalize(),
						activator.getComponent(TransformComponent.class).getPosition().distance(entityPos),
						List.of(PhysicsLayer.OBSTACLE)
				);

				// If a non-wall was hit then apply damage
				if (!raycastHit.isHit()) {
					if (entity.hasComponent(StatsComponent.class)) {
						float currentHealth = entity.getComponent(StatsComponent.class).getCurrentHealth();
						entity.getComponent(StatsComponent.class).setCurrentHealth(currentHealth - 1);
					}
				}
				}
			}

			// clean up the entity after the animation via a forked process
			// execution is not done ScheduledExecutorService as it is a
			// oneoff task,
			// else the scheduled exectutor intance would have to passed to each
			// weapon instance.
			Thread thread = new Thread(new Runnable() {
				@Override
				public synchronized void run() {
					try {
						wait(500);
						Scene.getActiveScene().removeEntity(animationEntity);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
			);
			// do not leave thread dangleling if jvm closes
			thread.setDaemon(true);
			thread.start();

	}
		private PhysicsLayer resolvePhysicsLayer (Entity activator){
			if (activator.hasComponent(PlayerComponent.class)) {
				return PhysicsLayer.ENEMY;
			} else return PhysicsLayer.PLAYER;

	}

		@Override
		public String getId () {
			return "melee_sweep";
		}
	}

