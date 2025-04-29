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

	// Added for smooth path following - tolerance for reaching waypoints
	private float waypointReachedTolerance = 0.2f;

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

	/**
	 * Get the next waypoint after the current one, useful for smooth path following
	 * @return The next waypoint if available
	 */
	public Optional<Vector2D> next() {
		int nextIndex = currentPathIndex + 1;
		return nextIndex < pathfindingRoute.size() ?
			Optional.of(pathfindingRoute.get(nextIndex)) :
			Optional.empty();
	}

	/**
	 * Returns true if the entity is close enough to the current waypoint to advance
	 * @param position Current world position
	 * @param currentWaypoint Current waypoint
	 * @return True if the waypoint is considered reached
	 */
	public boolean isWaypointReached(Vector2D position, Vector2D currentWaypoint) {
		return Vector2D.euclidean_distance(position, currentWaypoint) < waypointReachedTolerance;
	}

	/**
	 * Set the tolerance for considering a waypoint as reached
	 * @param tolerance The distance threshold
	 */
	public void setWaypointReachedTolerance(float tolerance) {
		this.waypointReachedTolerance = tolerance;
	}

	/**
	 * Get the current route
	 * @return The pathfinding route
	 */
	public List<Vector2D> getRoute() {
		return pathfindingRoute;
	}

	/**
	 * Get the current path index
	 * @return The index of the current waypoint
	 */
	public int getCurrentPathIndex() {
		return currentPathIndex;
	}

	/**
	 * Calculate a lookahead point for smoother path following
	 * This helps entities anticipate turns and curve naturally
	 * @param currentPosition Current entity position
	 * @param lookaheadDistance Distance to look ahead on the path
	 * @return A position to steer toward for smooth movement
	 */
	// Violates ECS by components not being data only
	public Optional<Vector2D> calculateLookaheadPoint(Vector2D currentPosition, float lookaheadDistance) {
		if (pathfindingRoute.isEmpty() || currentPathIndex >= pathfindingRoute.size()) {
			return Optional.empty();
		}

		// Start with the current waypoint
		Vector2D currentWaypoint = pathfindingRoute.get(currentPathIndex);
		float distanceCovered = 0;
		int waypointIndex = currentPathIndex;

		// Find the point that's lookaheadDistance away along the path
		while (distanceCovered < lookaheadDistance && waypointIndex < pathfindingRoute.size() - 1) {
			Vector2D nextWaypoint = pathfindingRoute.get(waypointIndex + 1);
			float segmentLength = Vector2D.euclidean_distance(currentWaypoint, nextWaypoint);

			if (distanceCovered + segmentLength >= lookaheadDistance) {
				// Interpolate to find the exact point
				float remainingDistance = lookaheadDistance - distanceCovered;
				float t = remainingDistance / segmentLength;
				return Optional.of(currentWaypoint.lerp(nextWaypoint, t));
			}

			distanceCovered += segmentLength;
			currentWaypoint = nextWaypoint;
			waypointIndex++;
		}

		// If we got to the end of the path, return the last point
		return Optional.of(pathfindingRoute.get(pathfindingRoute.size() - 1));
	}
}