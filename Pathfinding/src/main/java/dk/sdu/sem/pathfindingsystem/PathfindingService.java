package dk.sdu.sem.pathfindingsystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.data.RaycastHit;
import dk.sdu.sem.commonpathfinding.IPathfindingSPI;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;

import java.util.List;
import java.util.ServiceLoader;

public class PathfindingService implements IPathfindingSPI {
	private ICollisionSPI collisionSPI;

	public PathfindingService() {
		collisionSPI = ServiceLoader.load(ICollisionSPI.class).findFirst().orElse(null);
	}

	@Override
	public boolean hasLineOfSight(Vector2D origin, Vector2D direction, Entity targetEntity, List<PhysicsLayer> obstacles) {
		if (collisionSPI == null) {
			collisionSPI = ServiceLoader.load(ICollisionSPI.class).findFirst().orElse(null);
		}

		if (collisionSPI == null) return false;

		RaycastHit hit = collisionSPI.raycast(origin, direction, 1000, obstacles);
		return hit.isHit() && hit.getEntity() == targetEntity;
	}

	@Override
	public Vector2D toWorldPosition(Vector2D position) {
		return position.scale((float) GameConstants.TILE_SIZE);
	}
}