package dk.sdu.sem.MeleeEnemy;

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
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.pathfindingsystem.PathfindingComponent;
import dk.sdu.sem.player.PlayerComponent;

//
public class MeleeEnemyFactory {

	public Entity create(Vector2D position, float moveSpeed, float friction,
						 int health,int attackSize){
		Entity enemy = new Entity();
		enemy.addComponent(new TransformComponent(position,0));
		enemy.addComponent(new PhysicsComponent(friction, 0.5f));
		// Deprecated in favour of statscomponent
		enemy.addComponent(new EnemyComponent(moveSpeed));
		enemy.addComponent(new PathfindingComponent( () -> {
			// TODO: optimize (scene entity traversal per half second per enemy)
			TransformComponent playerTransform = Scene.getActiveScene().getEntitiesWithComponent(PlayerComponent.class)
				.stream()
				.findFirst()
				.map(entity -> entity.getComponent(TransformComponent.class))
				.orElse(null);
			assert playerTransform != null;
			return java.util.Optional.ofNullable(playerTransform.getPosition());})
		);

		// Add weapon component
		IWeaponSPI weapon = WeaponRegistry.getWeapon("melee_sweep");
		if (weapon != null)
			// values below 50 do not have enough range to be impactfull
			enemy.addComponent(new WeaponComponent(weapon, 1, 2,60));

		// this is wrong that the sweeping enemy has same stats as a long
		// distance enemy.
		// this enemy would have different behavior to others wants to get as
		// close as
		// possible to player, should have 2 lives instead of 1 like a normal
		// shooting enemy
		// should have a bigger attacksize
		StatsComponent stats = StatsFactory.createStatsFor(enemy);
		stats.setDamage(1);
		stats.setMaxHealth(2);
		stats.setMoveSpeed(moveSpeed);

		// Set other stats
		stats.setBaseStat(StatType.DAMAGE, 15f);
		// this should be lower for the player
		stats.setBaseStat(StatType.ATTACK_RANGE, 10f);
		stats.setDefaultStat(StatType.ATTACK_SPEED,30);
		return enemy;
	}

}
