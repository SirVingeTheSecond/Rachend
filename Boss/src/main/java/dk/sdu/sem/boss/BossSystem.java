package dk.sdu.sem.boss;

import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.enemy.IEnemyFactory;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.services.IStart;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.Optional;
import java.util.ServiceLoader;

public class BossSystem implements IUpdate, IStart {
	private static IEnemyFactory enemyFactory;


	@Override
	public void update() {
		NodeManager.active().getNodes(BossNode.class).forEach(node -> {
			float hp = node.stats.getStat(StatType.CURRENT_HEALTH);
			float maxHealth = node.stats.getStat(StatType.MAX_HEALTH);

			if (!node.boss.summon1 && hp <= maxHealth/2) {
				summonEnemies(node);
				node.boss.summon1 = true;
			} else if (!node.boss.summon2 && hp <= maxHealth/4) {
				summonEnemies(node);
				node.boss.summon2 = true;
			}
		});
	}

	private void summonEnemies(BossNode node) {
		if (enemyFactory == null)
			return;

		node.boss.getSummonZones().forEach(summonZone -> {
			Entity summon = enemyFactory.create(summonZone.getPosition(), 100, 5, 3);
			node.getEntity().getScene().addEntity(summon);
		});
	}


	@Override
	public void start() {
		enemyFactory = ServiceLoader.load(IEnemyFactory.class).findFirst().orElse(null);
	}
}
