package dk.sdu.sem.pathfindingsystem;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Timer;

import java.util.Optional;

public class PathfindingComponent implements IComponent {
	public PathfindingTargetProvider targetProvider;
	public Timer refreshTimer = new Timer(0.5f);

	public PathfindingComponent(PathfindingTargetProvider targetProvider) {
		this.targetProvider = targetProvider;
	}

	public static PathfindingComponent empty() {
		return new PathfindingComponent(Optional::empty);
	}
}
