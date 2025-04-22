package dk.sdu.sem.pathfindingsystem;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Timer;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PathfindingComponent implements IComponent {
	public IPathfindingTargetProvider targetProvider;
	private List<Vector2D> pathfindingRoute = new ArrayList<>();
	public Timer refreshTimer = new Timer(0.5f);
	private int currentPathIndex = 0;

	public void setRoute(List<Vector2D> newPath) {
		// If we have a current position in the existing path, try to find the closest point in new path
		if (!pathfindingRoute.isEmpty() && currentPathIndex < pathfindingRoute.size()) {
			Vector2D currentTarget = pathfindingRoute.get(currentPathIndex);

			// Find the closest point in the new path
			int closestIndex = 0;
			float closestDistance = Float.MAX_VALUE;

			for (int i = 0; i < newPath.size(); i++) {
				float distance = Vector2D.euclidean_distance(currentTarget, newPath.get(i));
				if (distance < closestDistance) {
					closestDistance = distance;
					closestIndex = i;
				}
			}

			this.pathfindingRoute = newPath;
			this.currentPathIndex = closestIndex;
		} else {
			// Default behavior for first path
			this.pathfindingRoute = newPath;
			this.currentPathIndex = 0;
		}
	}

	public PathfindingComponent(IPathfindingTargetProvider targetProvider) {
		this.targetProvider = targetProvider;
	}

	public Optional<Vector2D> current() {
		return currentPathIndex < pathfindingRoute.size() ? Optional.of(pathfindingRoute.get(currentPathIndex)) : Optional.empty();
	}

	public void advance() {
		if (currentPathIndex < pathfindingRoute.size()) {
			currentPathIndex++;
		}
	}

	public List<Vector2D> getRoute() {
		return pathfindingRoute;
	}
}