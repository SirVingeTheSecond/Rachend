package dk.sdu.sem.BulletSystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweaponsystem.IWeaponSPI;
import dk.sdu.sem.commonweaponsystem.WeaponComponent;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.scenes.SceneManager;

public class BulletWeapon implements IWeaponSPI {
	// spawn a BulletNode, which can be observed by BulletSystem
	@Override
	public void activateWeapon(Entity activator, Vector2D direction) {
		WeaponComponent weaponComponent = activator.getComponent(WeaponComponent.class);
		if (Time.getTime() - weaponComponent.lastActivated > weaponComponent.getAttackTimer()) {
			weaponComponent.lastActivated = Time.getTime();
			TransformComponent activatorTransformer = activator.getComponent(TransformComponent.class);

			Entity entity = createBullet(activatorTransformer.getPosition(), direction.angle(), activator);
			SceneManager.getInstance().getActiveScene().addEntity(entity);
// TODO
			// update the weapon cooldown for the component of the entity which
			// fired it
//		activator.getComponent(WeaponComponent.class)
//		weapon.timer -= (float) Time.getDeltaTime();
//		weapon.timer = Math.max(0, weapon.timer);
		}
	}

	private Entity createBullet(Vector2D position, float rotation,Entity activator) {
		Entity entity = new Entity();
		BulletComponent bulletComponent = new BulletComponent();
		entity.addComponent(bulletComponent);

		PhysicsComponent physicsComponent = new PhysicsComponent(0);
		Vector2D velocity = new Vector2D(1,0).rotate(rotation).scale(bulletComponent.getSpeed());
		physicsComponent.setVelocity(activator.getComponent(PhysicsComponent.class).getVelocity().scale(0.1f).add(velocity));

		entity.addComponent(physicsComponent);

		// TODO offset this spawned bullet from player location a bit.
		entity.addComponent(new TransformComponent(position, physicsComponent.getVelocity().angle()));

		System.out.println("ANGLE: " + rotation);

		SpriteRendererComponent spriteRenderer = new SpriteRendererComponent();
		spriteRenderer.setRenderLayer(GameConstants.LAYER_CHARACTERS);
		entity.addComponent(spriteRenderer);

		AnimatorComponent animator = new AnimatorComponent("fire_bullet_anim");
		entity.addComponent(animator);
//		entity.addComponent(new PhysicsComponent(1.1f));
		return entity;
	}
}

