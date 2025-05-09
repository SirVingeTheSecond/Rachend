package dk.sdu.sem.boss;

import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.gamesystem.services.IUpdate;

public class BossSystem implements IUpdate {
	private boolean spawnedEnemies;

	@Override
	public void update() {
		NodeManager.active().getNodes(BossNode.class).forEach(node -> {

			float maxHP = node.stats.getMaxHealth();
			float health = node.stats.getCurrentHealth();

			if (!spawnedEnemies && health < maxHP / 2) {


				spawnedEnemies = true;
			}
		});
	}
}
