package dk.sdu.sem.pathfindingsystem;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.Optional;
import java.util.Set;

public class PathfindingSystem implements IUpdate {
	@Override
	public void update() {
		Set<PathfindingNode> pathfindingNodes = NodeManager.active().getNodes(PathfindingNode.class);

		for (PathfindingNode pathfindingNode : pathfindingNodes) {
			PathfindingComponent pathfindingComponent = pathfindingNode.pathfindingComponent;

			if (pathfindingComponent.refreshTimer.update((float)Time.getDeltaTime())) {
				Optional<Vector2D> target = pathfindingComponent.targetProvider.getTarget();

				System.out.println("PathfindingSystem::update -> target = " + target);
			}
		}
	}
}
