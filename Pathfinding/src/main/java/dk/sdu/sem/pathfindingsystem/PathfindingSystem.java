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
	private static Vector2D[] directions;

	public PathfindingSystem() {
		directions = new Vector2D[] {
			Vector2D.UP,
			Vector2D.DOWN,
			Vector2D.LEFT,
			Vector2D.RIGHT,
			Vector2D.UP.add(Vector2D.LEFT),
			Vector2D.UP.add(Vector2D.RIGHT),
			Vector2D.DOWN.add(Vector2D.LEFT),
			Vector2D.DOWN.add(Vector2D.RIGHT),
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

	private static List<Vector2D> findPath(Vector2D start, Vector2D target, Function<Vector2D, Boolean> sampleGrid) {
		PriorityQueue<PathNode> unexploredSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fCost));
		Set<Vector2D> visited = new HashSet<>();

		Map<Vector2D, PathNode> allNodes = new HashMap<>();

		PathNode startNode = new PathNode(start, null, target);
		unexploredSet.add(startNode);

		allNodes.put(start, startNode);

		while (!unexploredSet.isEmpty()) {
			PathNode current = unexploredSet.poll();

			// Check if we reached the target
			if (current.position.equals(target)) {
				return reconstructPath(current);
			}

			visited.add(current.position);

			// Expand neighbors
			for (Vector2D direction : directions) {
				Vector2D neighborPos = current.position.add(direction);

				// Skip if the neighbor is an obstacle or already visited
				if (sampleGrid.apply(neighborPos) || visited.contains(neighborPos)) {
					continue;
				}

				float preliminaryGCost = current.gCost + Vector2D.euclidean_distance(current.position, neighborPos);
				PathNode neighborNode = allNodes.get(neighborPos);

				if (neighborNode == null) {
					// Create new neighbor
					neighborNode = new PathNode(neighborPos, current, target);
					allNodes.put(neighborPos, neighborNode);
					unexploredSet.add(neighborNode);
				} else if (preliminaryGCost < neighborNode.gCost) {
					// Update the node with a better cost if found
					neighborNode.parent = current;
					neighborNode.gCost = preliminaryGCost;
					neighborNode.fCost = neighborNode.gCost + neighborNode.hCost;

					/*
					Instead of removing and re-adding nodes when a better path is found,
					we can simply insert a new node with the updated cost (even if an older version still exists in the queue).
					When a node is eventually polled from the queue, we check if its cost is still the best available
					 */

					// ToDo: We should consider lazy deletion to allow duplicates in the priority queue instead of removing and re-adding the node.
					// Removing a node from a PriorityQueue has O(n) time complexity, and lazy deletion could theoretically improve performance.
					// It is not optimal space complexity wise, and Rolf has told us that space complexity > time complexity for most instances (relatively).

					// Update the priority queue, remove and re-add the neighbor
					unexploredSet.remove(neighborNode);
					unexploredSet.add(neighborNode);
				}
			}
		}
		// No path found, return an empty path
		return new ArrayList<>();
	}

	private static void updatePathfindingNode(PathfindingNode pathfindingNode, Function<Vector2D, Boolean> sampleGrid) {
		// Only update the path if the timer is due
		if (!pathfindingNode.pathfindingComponent.refreshTimer.update((float) Time.getDeltaTime())) {
			return;
		}

		Vector2D startGridPosition = toGridPosition(pathfindingNode.transform.getPosition());
		Vector2D targetGridPosition = toGridPosition(pathfindingNode.pathfindingComponent.targetProvider.getTarget().orElseThrow());

		List<Vector2D> path = findPath(startGridPosition, targetGridPosition, sampleGrid);
		pathfindingNode.pathfindingComponent.setRoute(path);
	}

	@Override
	public void update() {
		Set<Entity> tilemapColliderEntities = SceneManager.getInstance().getActiveScene().getEntitiesWithComponent(TilemapColliderComponent.class);
		Set<TilemapColliderComponent> tilemapColliderComponents = tilemapColliderEntities.stream()
				.map(entity -> entity.getComponent(TilemapColliderComponent.class))
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

		// The sampleGrid function should return true if the grid position is blocked
		Function<Vector2D, Boolean> sampleGrid = gridPosition -> tilemapColliderComponents.stream()
				.filter(tilemap -> tilemap.getLayer() == PhysicsLayer.OBSTACLE)
				.anyMatch(tilemap -> tilemap.isSolid((int) gridPosition.x(), (int) gridPosition.y()));

		Set<PathfindingNode> pathfindingNodes = NodeManager.active().getNodes(PathfindingNode.class);
		pathfindingNodes.forEach(node -> updatePathfindingNode(node, sampleGrid));
	}

	@Override
	public void onGUI(GraphicsContext gc) {
		Set<PathfindingNode> pathfindingNodes = NodeManager.active().getNodes(PathfindingNode.class);
		pathfindingNodes.forEach(node -> {
			// draw the route as a neon green line
			gc.setStroke(Color.GREEN);
			gc.setLineWidth(5);
			List<Vector2D> route = node.pathfindingComponent.getRoute();
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
		});
	}
}
