package dk.sdu.sem.pathfindingsystem;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Timer;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PathfindingComponent implements IComponent {
	public PathfindingTargetProvider targetProvider;
	private List<Vector2D> pathfindingRoute = new ArrayList<>();
	public Timer refreshTimer = new Timer(0.5f);
	private int currentPathIndex = 0;

	public void setRoute(List<Vector2D> pathfindingRoute) {
		this.pathfindingRoute = pathfindingRoute;
		this.currentPathIndex = 0;
	}

	public PathfindingComponent(PathfindingTargetProvider targetProvider) {
		this.targetProvider = targetProvider;
	}

	public static PathfindingComponent empty() {
		return new PathfindingComponent(Optional::empty);
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
