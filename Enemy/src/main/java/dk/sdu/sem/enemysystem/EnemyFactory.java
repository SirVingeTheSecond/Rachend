package dk.sdu.sem.enemysystem;

import dk.sdu.sem.commonhealth.HealthComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweaponsystem.IBulletWeapon;
import dk.sdu.sem.commonweaponsystem.WeaponComponent;
import dk.sdu.sem.enemy.EnemyComponent;
import dk.sdu.sem.enemy.IEnemyFactory;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.assets.references.SpriteReference;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.rendering.Sprite;

import java.util.ServiceLoader;

public class EnemyFactory implements IEnemyFactory {

	/**
	 * Creates an enemy entity with default settings.
	 */
	@Override
	public Entity create() {
		return create(new Vector2D(500,400), 200.0f, 5.0f, 50);
	}

	/**
	 * Creates an enemy entity with custom settings.
	 */
	@Override
	public Entity create(Vector2D position, float moveSpeed, float friction, int health) {
		Entity enemy = new Entity();

		// Core components for an enemy
		enemy.addComponent(new TransformComponent(position, 0, new Vector2D(2,2)));
		enemy.addComponent(new PhysicsComponent(friction));
		enemy.addComponent(new EnemyComponent(moveSpeed));
		enemy.addComponent(new HealthComponent(health));

		IAssetReference<Sprite> defaultSpriteRef = new SpriteReference("big_demon_idle_anim_f0");

		// Sprite renderer and first animation frame
		SpriteRendererComponent renderer = new SpriteRendererComponent(defaultSpriteRef);
		renderer.setRenderLayer(GameConstants.LAYER_CHARACTERS);
		enemy.addComponent(renderer);
		ServiceLoader<IBulletWeapon> weaponloader = ServiceLoader.load(IBulletWeapon.class);
		IBulletWeapon weapon = weaponloader.iterator().next();
		enemy.addComponent(new WeaponComponent(weapon,1,0,4));

		// Animator component
		AnimatorComponent animator = new AnimatorComponent();

		// Animation states added
		animator.addState("idle", "demon_idle");
		animator.addState("run", "demon_run");

		// Initial state of animation
		animator.setCurrentState("idle");

		// Transitions between states
		animator.addTransition("idle", "run", "isMoving", true);
		animator.addTransition("run", "idle", "isMoving", false);

		enemy.addComponent(animator);

		return enemy;
	}
}
