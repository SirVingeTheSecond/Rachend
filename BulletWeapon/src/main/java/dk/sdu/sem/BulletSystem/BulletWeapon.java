package dk.sdu.sem.BulletSystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweaponsystem.IWeapon;
//import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.scenes.SceneManager;

public class BulletWeapon implements IWeapon {
	// spawn a BulletNode, which can be observed by BulletSystem
	@Override
	public void activateWeapon(Entity activator, Vector2D direction) {

		TransformComponent activatorTransformer =
			activator.getComponent(TransformComponent.class);
//		Vector2D offsatPosition =
//			activatorTransformer.getPosition().add(new Vector2D(4.0f,4.0f));
		Entity entity = createBullet(activatorTransformer.getPosition(),
			direction.normalize().angle(),
		activator);
		SceneManager.getInstance().getActiveScene().addEntity(entity);
// TODO
		// update the weapon cooldown for the component of the entity which
		// fired it
//		activator.getComponent(WeaponComponent.class)
//		weapon.timer -= (float) Time.getDeltaTime();
//		weapon.timer = Math.max(0, weapon.timer);
	}

	private Entity createBullet(Vector2D position, float rotation,Entity activator) {
		Entity entity = new Entity();
		entity.addComponent(new BulletComponent());
		// TODO offset this spawned bullet from player location a bit.
		entity.addComponent(new TransformComponent(position,rotation));
//		entity.addComponent(new PhysicsComponent(1.1f));
		return entity;
	}
}
