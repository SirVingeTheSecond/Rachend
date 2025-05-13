package dk.sdu.sem.pathfindingsystem;

import dk.sdu.sem.collision.components.TilemapColliderComponent;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import dk.sdu.sem.gamesystem.services.IUpdate;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PathfindingSystem implements IUpdate, IGUIUpdate {
	private static Vector2D[] cardinalDirections; // Manhattan distance
	private static Vector2D[] diagonalDirections;

	public PathfindingSystem() {
		// Define cardinal directions (non-diagonal)
		cardinalDirections = new Vector2D[] {
			Vector2D.UP,
			Vector2D.DOWN,
			Vector2D.LEFT,
			Vector2D.RIGHT
		};

		// Define diagonal directions separately
		diagonalDirections = new Vector2D[] {
			Vector2D.UP.add(Vector2D.LEFT),
			Vector2D.UP.add(Vector2D.RIGHT),
			Vector2D.DOWN.add(Vector2D.LEFT),
			Vector2D.DOWN.add(Vector2D.RIGHT)
		};
	}

	private static Vector2D toGridPosition(Vector2D position) {
		return position.scale((float) 1 / GameConstants.TILE_SIZE).floor();
	}

	private static Vector2D toWorldPosition(Vector2D position) {
		return position.scale((float) GameConstants.TILE_SIZE);
	}

	// Reconstruct path by doing the backwards from goal node
	private static List<Vector2D> reconstructPath(PathNode goalNode) {
		List<Vector2D> path = new ArrayList<>();
		PathNode current = goalNode;
		while (current != null) {
			path.add(current.position);
			current = current.parent;
		}
		Collections.reverse(path);
		return path;
	}

	/**
	 * Determines if a diagonal move is valid by checking adjacent tiles
	 * For a diagonal move to be valid, both adjacent tiles must be walkable
	 * @param position Current position
	 * @param direction Diagonal direction vector
	 * @param sampleGrid Function to check if a tile is an obstacle
	 * @return true if the diagonal move is valid
	 */
	private static boolean isDiagonalMoveValid(Vector2D position, Vector2D direction, Function<Vector2D, Boolean> sampleGrid) {
		// Decompose diagonal direction into cardinal components
		Vector2D horizontalComponent = new Vector2D(direction.x(), 0);
		Vector2D verticalComponent = new Vector2D(0, direction.y());

		// Check if both adjacent tiles are walkable
		boolean horizontalClear = !sampleGrid.apply(position.add(horizontalComponent));
		boolean verticalClear = !sampleGrid.apply(position.add(verticalComponent));

		// A diagonal move is only valid if both adjacent tiles are clear
		return horizontalClear && verticalClear;
	}

	/**
	 * Calculate a tile's proximity score to obstacles. Lower is better (further from obstacles).
	 * @param position The position to check
	 * @param sampleGrid Function to check if a tile is an obstacle
	 * @return A score indicating how close the position is to obstacles
	 */
	private static float calculateObstacleProximity(Vector2D position, Function<Vector2D, Boolean> sampleGrid) {
		float proximityScore = 0.0f;

		// Check surrounding tiles in a 3x3 grid
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				// Skip the center tile (position itself)
				if (dx == 0 && dy == 0) continue;

				Vector2D checkPos = position.add(new Vector2D(dx, dy));

				// If the tile is an obstacle, add to proximity score based on distance
				if (sampleGrid.apply(checkPos)) {
					// Calculate squared distance (faster than euclidean)
					float distance = dx*dx + dy*dy;

					// Closer obstacles have more influence
					proximityScore += 1.0f / Math.max(0.5f, distance);
				}
			}
		}

		return proximityScore;
	}

	private static List<Vector2D> findPath(Vector2D start, Vector2D target, Function<Vector2D, Boolean> sampleGrid) {
		PriorityQueue<PathNode> unexploredSet = new PriorityQueue<PathNode>(
			new Comparator<PathNode>() {
				@Override
				public int compare(PathNode o1, PathNode o2) {
					// prioritize lower fCost, then lower hCost
					return Float.compare(o1.fCost, o2.fCost) > 0 ?
						Float.compare(o1.hCost, o2.hCost) : -1;
				}
			}
		);
		Set<Vector2D> visited = new HashSet<>();

		Map<Vector2D, PathNode> allNodes = new HashMap<>();

		// No obstacle penalty for start node
		PathNode startNode = new PathNode(start, null, target, 0);
		unexploredSet.add(startNode);

		allNodes.put(start, startNode);

		// Constant for wall avoidance - higher values keep entities further from walls
		final float WALL_AVOIDANCE_WEIGHT = 2.0f;

		while (!unexploredSet.isEmpty()) {
			PathNode current = unexploredSet.poll();

			// Check if we reached the target
			if (current.position.equals(target)) {
				return smoothPath(reconstructPath(current), sampleGrid);
			}

			visited.add(current.position);

			// First explore cardinal directions
			for (Vector2D direction : cardinalDirections) {
				Vector2D neighborPos = current.position.add(direction);

				// Skip if the neighbor is an obstacle or already visited
				if (sampleGrid.apply(neighborPos) || visited.contains(neighborPos)) {
					continue;
				}

				// Calculate obstacle proximity score
				float obstacleProximity = calculateObstacleProximity(neighborPos, sampleGrid) * WALL_AVOIDANCE_WEIGHT;

				processNeighbor(current, neighborPos, target, obstacleProximity, unexploredSet, visited, allNodes);
			}

			// Then explore diagonal directions if valid
			for (Vector2D direction : diagonalDirections) {
				Vector2D neighborPos = current.position.add(direction);

				// Skip if the neighbor is an obstacle or already visited
				if (sampleGrid.apply(neighborPos) || visited.contains(neighborPos)) {
					continue;
				}

				// Additional check for diagonal movement
				if (!isDiagonalMoveValid(current.position, direction, sampleGrid)) {
					continue;
				}

				// Calculate obstacle proximity score - diagonal moves near walls are more penalized
				float obstacleProximity = calculateObstacleProximity(neighborPos, sampleGrid) * WALL_AVOIDANCE_WEIGHT * 1.5f;

				processNeighbor(current, neighborPos, target, obstacleProximity, unexploredSet, visited, allNodes);
			}
		}
		// No path found, return an empty path
		return new ArrayList<>();
	}

	/**
	 * Process a neighbor node in the A* algorithm
	 */
	private static void processNeighbor(
		PathNode current,
		Vector2D neighborPos,
		Vector2D target,
		float obstacleProximity,
		PriorityQueue<PathNode> unexploredSet,
		Set<Vector2D> visited,
		Map<Vector2D, PathNode> allNodes
	) {
		// Base path cost (distance)
		float baseGCost = current.gCost + Vector2D.euclidean_distance(current.position, neighborPos);

		// Account for obstacle proximity when comparing paths
		float preliminaryGCost = baseGCost + obstacleProximity;

		PathNode neighborNode = allNodes.get(neighborPos);

		if (neighborNode == null) {
			// Create new neighbor with obstacle proximity penalty
			neighborNode = new PathNode(neighborPos, current, target, obstacleProximity);
			allNodes.put(neighborPos, neighborNode);
			unexploredSet.add(neighborNode);
		} else if (preliminaryGCost < neighborNode.gCost) {
			// Update the node with a better cost if found
			neighborNode.parent = current;
			neighborNode.gCost = preliminaryGCost;
			neighborNode.fCost = neighborNode.gCost + neighborNode.hCost;

			// Update the priority queue, remove and re-add the neighbor
			unexploredSet.remove(neighborNode);
			unexploredSet.add(neighborNode);
		}
	}

	/**
	 * Smooths a path by adding additional points to create arc-like paths around corners
	 * @param path The original path
	 * @param sampleGrid Function to check if a tile is an obstacle
	 * @return A smoothed path with arc points around corners
	 */
	private static List<Vector2D> smoothPath(List<Vector2D> path, Function<Vector2D, Boolean> sampleGrid) {
		if (path.size() < 3) {
			return path; // Not enough points to smooth
		}

		List<Vector2D> smoothedPath = new ArrayList<>();
		smoothedPath.add(path.get(0)); // Add start point

		// Process corners in the path
		for (int i = 1; i < path.size() - 1; i++) {
			Vector2D prev = path.get(i - 1);
			Vector2D current = path.get(i);
			Vector2D next = path.get(i + 1);

			// Check if this is a corner (direction changes)
			Vector2D dirFromPrev = current.subtract(prev).normalize();
			Vector2D dirToNext = next.subtract(current).normalize();

			// If direction changes significantly and it's a clean corner, add arc points
			if (!dirFromPrev.equals(dirToNext) && Math.abs(dirFromPrev.dot(dirToNext)) < 0.9f) {
				// Add corner arc interpolation points
				addCornerArcPoints(smoothedPath, prev, current, next);
			} else {
				// Just add the current point
				smoothedPath.add(current);
			}
		}

		smoothedPath.add(path.get(path.size() - 1)); // Add end point
		return smoothedPath;
	}

	/**
	 * Adds intermediate points to create a smooth arc around a corner
	 */
	private static void addCornerArcPoints(List<Vector2D> smoothedPath, Vector2D prev, Vector2D corner, Vector2D next) {
		// Calculate center point for a circular arc
		Vector2D midPoint1 = prev.add(corner).scale(0.5f);
		Vector2D midPoint2 = corner.add(next).scale(0.5f);

		// Calculate control point for a quadratic Bézier curve
		// This creates a nice circular arc approximation
		Vector2D controlPoint = corner;

		// Add interpolated points to create a smooth curve
		final int ARC_SEGMENTS = 3; // Number of points to add for the arc

		for (int i = 1; i <= ARC_SEGMENTS; i++) {
			float t = i / (float)(ARC_SEGMENTS + 1);

			// Quadratic Bézier curve interpolation
			// Formula: B(t) = (1-t)^2 P₀ + 2(1-t)tP₁ + t^2P₂
			float oneMinusT = 1.0f - t;

			Vector2D p0 = midPoint1;
			Vector2D p1 = controlPoint;
			Vector2D p2 = midPoint2;

			Vector2D arcPoint = p0.scale(oneMinusT * oneMinusT)
				.add(p1.scale(2 * oneMinusT * t))
				.add(p2.scale(t * t));

			smoothedPath.add(arcPoint);
		}
	}

	private static void updatePathfindingNode(PathfindingNode node,
											  Function<Vector2D, Boolean> sampleGrid) {
		if (!node.pathfindingComponent.refreshTimer
			.update((float) Time.getDeltaTime())) {
			return;
		}

		Optional<Vector2D> optTarget =
			node.pathfindingComponent.targetProvider.getTarget();

		if (optTarget.isEmpty()) {
			// No valid target -> clear any existing route
			node.pathfindingComponent.setRoute(Collections.emptyList());
			return;
		}

		Vector2D start = toGridPosition(node.transform.getPosition());
		Vector2D goal  = toGridPosition(optTarget.get());

		List<Vector2D> path = findPath(start, goal, sampleGrid);
		node.pathfindingComponent.setRoute(path);
	}

	@Override
	public void update() {
		Set<Entity> colliders = SceneManager.getInstance()
			.getActiveScene()
			.getEntitiesWithComponent(TilemapColliderComponent.class);

		Set<TilemapColliderComponent> tilemaps = colliders.stream()
			.map(e -> e.getComponent(TilemapColliderComponent.class))
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		Function<Vector2D, Boolean> sampleGrid = gp -> tilemaps.stream()
			.filter(t -> t.getLayer() == PhysicsLayer.OBSTACLE
				|| t.getLayer() == PhysicsLayer.HOLE)
			.anyMatch(t -> t.isSolid((int) gp.x(), (int) gp.y()));

		NodeManager.active()
			.getNodes(PathfindingNode.class)
			.forEach(n -> updatePathfindingNode(n, sampleGrid));
	}

	@Override
	public void onGUI(GraphicsContext gc) {
		Set<PathfindingNode> pathfindingNodes = NodeManager.active().getNodes(PathfindingNode.class);
		pathfindingNodes.forEach(node -> {
			// Draw the route as a neon green line
			gc.setStroke(Color.GREEN);
			gc.setLineWidth(5);
			List<Vector2D> route = node.pathfindingComponent.getRoute();

			// Only try to draw if there are at least 2 points
			if (route.size() >= 2) {
				gc.strokePolyline(
					route.stream()
						.map(v -> v.add(new Vector2D(0.5f, 0.5f)))
						.map(PathfindingSystem::toWorldPosition)
						.map(Vector2D::x)
						.mapToDouble(Double::valueOf)
						.toArray(),
					route.stream()
						.map(v -> v.add(new Vector2D(0.5f, 0.5f)))
						.map(PathfindingSystem::toWorldPosition)
						.map(Vector2D::y)
						.mapToDouble(Double::valueOf)
						.toArray(),
					route.size()
				);

				// Draw small circles at each point to visualize the path better
				gc.setFill(Color.BLUE);
				route.stream()
					.map(v -> v.add(new Vector2D(0.5f, 0.5f)))
					.map(PathfindingSystem::toWorldPosition)
					.forEach(p -> {
						gc.fillOval(p.x() - 3, p.y() - 3, 6, 6);
					});
			}
		});
	}
}