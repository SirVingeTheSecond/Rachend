package dk.sdu.sem.commonpathfinding;

import dk.sdu.sem.commonsystem.Vector2D;

import java.util.Optional;

public interface IPathfindingTargetProvider {
	Optional<Vector2D> getTarget();
}
