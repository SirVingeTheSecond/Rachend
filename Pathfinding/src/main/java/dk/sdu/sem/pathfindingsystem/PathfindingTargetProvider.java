package dk.sdu.sem.pathfindingsystem;

import dk.sdu.sem.commonsystem.Vector2D;

import java.util.Optional;

public interface PathfindingTargetProvider {
	Optional<Vector2D> getTarget();
}
