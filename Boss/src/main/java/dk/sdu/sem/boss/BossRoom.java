package dk.sdu.sem.boss;

import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.collision.components.CollisionStateComponent;
import dk.sdu.sem.commonlevel.room.IRoomCreatedListener;
import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonstats.StatsFactory;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.IWeaponSPI;
import dk.sdu.sem.commonweapon.WeaponComponent;
import dk.sdu.sem.commonweapon.WeaponRegistry;
import dk.sdu.sem.enemy.EnemyComponent;
import dk.sdu.sem.enemy.IEnemyFactory;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.assets.references.SpriteReference;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.commonpathfinding.PathfindingComponent;
import dk.sdu.sem.player.PlayerComponent;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

public class BossRoom implements IRoomCreatedListener {
	@Override
	public void onRoomCreated(Room room) {
		List<Room.Zone> bossZones = room.getZones("BOSS");
		if (bossZones.isEmpty())
			return;

		if (ServiceLoader.load(IEnemyFactory.class).findFirst().isEmpty())
			return;

		Room.Zone bossZone = bossZones.get(0);
		Entity boss = createBoss(bossZone, room);
		room.getScene().addEntity(boss);
	}

	private Entity createBoss(Room.Zone bossZone, Room room) {
		Entity enemy = new Entity();

		// Core components for an enemy
		enemy.addComponent(new TransformComponent(bossZone.getPosition(), 0, new Vector2D(3,3)));
		enemy.addComponent(new PhysicsComponent(5, 100f));
		enemy.addComponent(new EnemyComponent());
		enemy.addComponent(new PathfindingComponent(() -> {
			// TODO: optimize (scene entity traversal per half second per enemy)
			TransformComponent playerTransform = Scene.getActiveScene().getEntitiesWithComponent(PlayerComponent.class)
				.stream()
				.findFirst()
				.map(entity -> entity.getComponent(TransformComponent.class))
				.orElse(null);

			return Optional.ofNullable(playerTransform).map(TransformComponent::getPosition);
		}));

		// Add unified stats component using the factory
		StatsComponent stats = StatsFactory.createStatsFor(enemy);

		stats.setBaseStat(StatType.MAX_HEALTH, 100f);
		stats.setBaseStat(StatType.CURRENT_HEALTH, 100f);

		// Set other stats
		stats.setBaseStat(StatType.ATTACK_RANGE, 1000000f);

		// Add weapon component
		IWeaponSPI weapon = WeaponRegistry.getWeapon("boss_weapon");
		if (weapon != null)
			enemy.addComponent(new WeaponComponent(stats, List.of(weapon)));

		// Setup sprite renderer
		IAssetReference<Sprite> defaultSpriteRef = new SpriteReference("big_demon_idle_anim_f0_sprite");
		SpriteRendererComponent renderer = new SpriteRendererComponent(defaultSpriteRef);
		renderer.setRenderLayer(GameConstants.LAYER_OBJECTS);
		enemy.addComponent(renderer);

		// Setup animator component
		AnimatorComponent animator = new AnimatorComponent();

		// Add animation states
		animator.addState("idle", "boss_idle");
		animator.addState("hurt", "boss_hurt");

		// Set initial state
		animator.setCurrentState("idle");

		stats.addStatChangeListener(StatType.CURRENT_HEALTH, (oldValue, newValue) -> {
			if (newValue < oldValue) {
				animator.setOneShotData("hurt", "idle");
			}
		});

		enemy.addComponent(animator);

		// Add a collider for the enemy
		enemy.addComponent(new CircleColliderComponent(enemy, 10));
		enemy.addComponent(new CollisionStateComponent());

		//Boss component
		enemy.addComponent(new BossComponent(room.getZones("BOSS_SUMMON")));

		return enemy;
	}
}
