package dk.sdu.sem.pathfindingsystem;

import dk.sdu.sem.collision.PhysicsLayer;
import dk.sdu.sem.collision.components.TilemapColliderComponent;
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

class PathNode {
	public Vector2D pathNodePosition;
	public Vector2D startPosition;
	public Vector2D targetPosition;
	public boolean visited;

	public PathNode(Vector2D pathNodePosition, Vector2D startPosition, Vector2D targetPosition, boolean visited) {
		this.pathNodePosition = pathNodePosition;
		this.startPosition = startPosition;
		this.targetPosition = targetPosition;
		this.visited = visited;
	}

	public int hCost() {
		return (int) Vector2D.euclidean_distance(pathNodePosition, targetPosition);
	}

	public int gCost() {
		return (int) Vector2D.euclidean_distance(pathNodePosition, startPosition);
	}

	public int fCost() {
		return gCost() + hCost();
	}

	@Override
	public String toString() {
		return "PathNode{" +
				"position=" + pathNodePosition +
				", fCost=" + fCost() +
				", gCost=" + gCost() +
				", hCost=" + hCost() +
				"}";
	}

	public Collection<PathNode> expand(Function<Vector2D,Boolean> sampleGrid, ArrayList<PathNode> pathNodeQueue) {
		Collection<PathNode> pathNodes = new ArrayList<>();

		for (Vector2D direction : Vector2D.DIRECTIONS) {
			Vector2D newPosition = pathNodePosition.add(direction);

			if (!sampleGrid.apply(newPosition) && pathNodeQueue.stream().noneMatch(pathNode -> pathNode.pathNodePosition.equals(newPosition))) {
				pathNodes.add(new PathNode(newPosition, startPosition, targetPosition, false));
			}
		}

		return pathNodes;
	}
}

public class PathfindingSystem implements IUpdate, IGUIUpdate {
	private static Vector2D toGridPosition(Vector2D position) {
		return position.scale((float) 1 / GameConstants.TILE_SIZE).floor();
	}

	private static Vector2D toWorldPosition(Vector2D position) {
		return position.scale((float) GameConstants.TILE_SIZE);
	}

	private static void updatePathfindingNode(PathfindingNode pathfindingNode, Function<Vector2D,Boolean> sampleGrid) {
		if (!pathfindingNode.pathfindingComponent.refreshTimer.update((float)Time.getDeltaTime())) { return; }

		Vector2D nodeGridPosition = toGridPosition(pathfindingNode.transform.getPosition());
		Vector2D targetGridPosition = toGridPosition(pathfindingNode.pathfindingComponent.targetProvider.getTarget().orElseThrow());

		ArrayList<PathNode> pathNodeQueue = new ArrayList<>();

		// add the surrounding tiles to the queue from the starting position
		pathNodeQueue.addAll(new PathNode(nodeGridPosition, nodeGridPosition, targetGridPosition, true).expand(sampleGrid, pathNodeQueue));

		ArrayList<PathNode> pathRoute = new ArrayList<>();

		boolean foundPath = false;
		while (foundPath == false || pathNodeQueue.isEmpty() == false) {
			Optional<PathNode> bestPathNode = pathNodeQueue.stream()
					.filter(pathNode -> pathNode.visited == false)
					.min(Comparator.comparingInt(PathNode::hCost))
					.stream()
					.min(Comparator.comparingInt(PathNode::fCost));

			if (bestPathNode.isPresent()) {
				PathNode currentPathNode = bestPathNode.get();
				currentPathNode.visited = true;

				if (currentPathNode.pathNodePosition.equals(targetGridPosition)) {
					foundPath = true;
					pathRoute.add(currentPathNode);
					break;
				}

				pathRoute.add(currentPathNode);
				pathNodeQueue.addAll(currentPathNode.expand(sampleGrid, pathNodeQueue));
			} else {
				break;
			}
		}

		// update the pathfinding component with the new path
		pathfindingNode.pathfindingComponent.setRoute(pathRoute.stream()
				.map(pathNode -> pathNode.pathNodePosition)
//				.map(PathfindingSystem::toWorldPosition)
				.collect(Collectors.toList()));
	}

	@Override
	public void update() {
		Set<Entity> tilemapColliderEntities = SceneManager.getInstance().getActiveScene().getEntitiesWithComponent(TilemapColliderComponent.class);
		Set<TilemapColliderComponent> tilemapColliderComponents = tilemapColliderEntities.stream()
				.map(entity -> entity.getComponent(TilemapColliderComponent.class))
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Function<Vector2D, Boolean> sampleGrid = gridPosition -> {
			return tilemapColliderComponents.stream()
					.filter(tilemap -> tilemap.getLayer() == PhysicsLayer.OBSTACLE)
					.anyMatch(tilemap -> tilemap.isSolid((int)gridPosition.x(), (int)gridPosition.y()));
		};

		Set<PathfindingNode> pathfindingNodes = NodeManager.active().getNodes(PathfindingNode.class);
		pathfindingNodes.forEach(node -> updatePathfindingNode(node, sampleGrid));
	}

	@Override
	public void onGUI(GraphicsContext gc) {
		Set<PathfindingNode> pathfindingNodes = NodeManager.active().getNodes(PathfindingNode.class);
		pathfindingNodes.forEach(node -> {
			Vector2D worldPosition = node.transform.getPosition();
			Vector2D gridPosition = toGridPosition(worldPosition);

			// draw a neon green line representing the pathfinding route
			gc.setStroke(Color.GREEN);
			gc.setLineWidth(5);
			gc.strokePolyline(
					node.pathfindingComponent.getRoute().stream()
							.map(v -> v.add(new Vector2D(0.5f, 0.5f)))
							.map(PathfindingSystem::toWorldPosition)
							.map(Vector2D::x)
							.mapToDouble(Double::valueOf)
							.toArray(),
					node.pathfindingComponent.getRoute().stream()
							.map(v -> v.add(new Vector2D(0.5f, 0.5f)))
							.map(PathfindingSystem::toWorldPosition)
							.map(Vector2D::y)
							.mapToDouble(Double::valueOf)
							.toArray(),
					node.pathfindingComponent.getRoute().size());
		});
	}
}
