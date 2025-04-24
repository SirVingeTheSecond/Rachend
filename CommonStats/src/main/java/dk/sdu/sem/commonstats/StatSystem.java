package dk.sdu.sem.commonstats;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.Set;

public class StatSystem implements IUpdate {
	@Override
	public void update() {
		Set<StatNode> statsNodes = NodeManager.active().getNodes(StatNode.class);

		for (StatNode node : statsNodes) {
			node.stats.updateModifiers();

			// Healing over time?
			// Damage over time?
		}
	}
}