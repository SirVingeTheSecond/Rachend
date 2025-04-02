package dk.sdu.sem.BulletSystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweaponsystem.IWeapon;
import dk.sdu.sem.commonweaponsystem.WeaponComponent;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.scenes.SceneManager;

public class BulletWeapon implements IWeapon {
	// spawn a BulletNode, which can be observed by BulletSystem
	@Override
	public void activateWeapon(Entity activator, Entity target,Vector2D direction) {
		Entity entity = new Entity();
		entity.addComponent(new BulletComponent());
		TransformComponent activatorTransformer =
			activator.getComponent(TransformComponent.class);
		// TODO offset this spawned bullet from player location a bit.
		entity.addComponent(new TransformComponent(activatorTransformer.getPosition().add(new Vector2D(4.0f,4.0f)),2.5f));
		SceneManager.getInstance().getActiveScene().addEntity(entity);

		// TODO
		// update the weapon cooldown for the component of the entity which
		// fired it
//		activator.getComponent(WeaponComponent.class)
//		weapon.timer -= (float) Time.getDeltaTime();
//		weapon.timer = Math.max(0, weapon.timer);
	}

}
