package dk.sdu.sem.pathfindingsystem;

import dk.sdu.sem.collision.components.TilemapColliderComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.Optional;
import java.util.Set;

public class PathfindingSystem implements IUpdate {
	@Override
	public void update() {
		Set<PathfindingNode> pathfindingNodes = NodeManager.active().getNodes(PathfindingNode.class);

		Set<Entity> tilemapColliderEntities = SceneManager.getInstance().getActiveScene().getEntitiesWithComponent(TilemapColliderComponent.class);


		for (PathfindingNode pathfindingNode : pathfindingNodes) {
			PathfindingComponent pathfindingComponent = pathfindingNode.pathfindingComponent;

			if (pathfindingComponent.refreshTimer.update((float)Time.getDeltaTime())) {
				Optional<Vector2D> target = pathfindingComponent.targetProvider.getTarget();

				target.ifPresent(position -> {
					Vector2D gridPosition = position.scale((float) 1 / GameConstants.TILE_SIZE).floor();
					System.out.println("PathfindingSystem::update -> target = " + gridPosition);

				});

			}
		}
	}
}
