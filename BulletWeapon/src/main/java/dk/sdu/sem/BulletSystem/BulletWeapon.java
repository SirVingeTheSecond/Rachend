package dk.sdu.sem.BulletSystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweaponsystem.IBulletWeapon;
import dk.sdu.sem.commonweaponsystem.WeaponComponent;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.scenes.SceneManager;

public class BulletWeapon implements IBulletWeapon {
	// spawn a BulletNode, which can be observed by BulletSystem
	@Override
	public void activateWeapon(Entity activator, Vector2D direction) {
		WeaponComponent weaponComponent =
			activator.getComponent(WeaponComponent.class);
		if ( Time.getTime() > weaponComponent.lastActivated ) {
			 weaponComponent.lastActivated =
				 // the cooldown is lower, while 7 is instant as 7-7 is 0
				 // maybe a better way to check if the attacktry violates the attack rate
				 Time.getTime()+7-weaponComponent.getAttackTimer();



		TransformComponent activatorTransformer =
			activator.getComponent(TransformComponent.class);

			Entity entity = createBullet(activatorTransformer.getPosition(),
				direction.normalize().angle(),
				activator);
			SceneManager.getInstance().getActiveScene().addEntity(entity);
		}
	}

	private Entity createBullet(Vector2D position, float rotation,Entity activator) {
			Entity entity = new Entity();
			entity.addComponent(new BulletComponent());
			// TODO offset this spawned bullet from player location a bit.
			entity.addComponent(new TransformComponent(position, rotation));
//		entity.addComponent(new PhysicsComponent(1.1f));
			return entity;
		}
	}

