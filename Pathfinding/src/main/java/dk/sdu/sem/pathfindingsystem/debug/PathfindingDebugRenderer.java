package dk.sdu.sem.pathfindingsystem.debug;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonsystem.debug.IPathfindingRenderer;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import dk.sdu.sem.pathfindingsystem.PathfindingNode;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Set;

/**
 * Visualizes pathfinding routes for debugging.
 */
public class PathfindingDebugRenderer implements IPathfindingRenderer {
	private static final Logging LOGGER = Logging.createLogger("PathfindingVisualizer", LoggingLevel.DEBUG);
	private static final int TILE_SIZE = GameConstants.TILE_SIZE;

	private static final Color PATH_COLOR = Color.GREEN;
	private static final Color WAYPOINT_COLOR = Color.BLUE;
	private static final Color START_COLOR = Color.LIMEGREEN;
	private static final Color GOAL_COLOR = Color.RED;
	private static final Color LABEL_COLOR = Color.WHITE;
	private static final Color ARROW_COLOR = Color.YELLOW;
	private static final Color CURRENT_WAYPOINT_COLOR = Color.CYAN;

	@Override
	public void drawPaths(GraphicsContext gc) {
		try {
			Set<PathfindingNode> pathfindingNodes = NodeManager.active().getNodes(PathfindingNode.class);

			LOGGER.debug("Drawing paths for " + pathfindingNodes.size() + " pathfinding nodes");

			if (pathfindingNodes.isEmpty()) {
				// Draw a message if no pathfinding nodes found
				gc.setFill(Color.WHITE);
				gc.fillText("No pathfinding entities in scene", 10, 60);
				return;
			}

			for (PathfindingNode node : pathfindingNodes) {
				List<Vector2D> route = node.pathfindingComponent.getRoute();
				drawPathfindingRoute(gc, route, node);
			}

		} catch (Exception e) {
			LOGGER.error("Error drawing paths: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void drawPathfindingRoute(GraphicsContext gc, List<Vector2D> route, PathfindingNode node) {
		// Only try to draw if there are at least 2 points
		if (route == null || route.size() < 2) {
			return;
		}

		// Draw the route as a line
		gc.setStroke(PATH_COLOR);
		gc.setLineWidth(3);

		// Convert grid positions to world positions and draw lines connecting them
		double[] xPoints = route.stream()
			.map(v -> v.add(new Vector2D(0.5f, 0.5f)))
			.map(this::toWorldPosition)
			.map(Vector2D::x)
			.mapToDouble(Double::valueOf)
			.toArray();

		double[] yPoints = route.stream()
			.map(v -> v.add(new Vector2D(0.5f, 0.5f)))
			.map(this::toWorldPosition)
			.map(Vector2D::y)
			.mapToDouble(Double::valueOf)
			.toArray();

		gc.strokePolyline(xPoints, yPoints, route.size());

		// Draw waypoints along the path
		drawWaypoints(gc, route);

		// Draw start and end points with special markers
		drawEndpoints(gc, route);

		// Draw the current waypoint the entity is moving toward
		drawCurrentWaypoint(gc, route, node);

		// Draw directional arrows along the path
		drawDirectionalArrows(gc, route);
	}

	private void drawWaypoints(GraphicsContext gc, List<Vector2D> route) {
		gc.setFill(WAYPOINT_COLOR);

		for (int i = 1; i < route.size() - 1; i++) {
			Vector2D pos = toWorldPosition(route.get(i).add(new Vector2D(0.5f, 0.5f)));
			gc.fillOval(pos.x() - 3, pos.y() - 3, 6, 6);

			// Draw waypoint indices
			gc.setFill(LABEL_COLOR);
			gc.fillText(String.valueOf(i), pos.x() + 5, pos.y() - 5);
			gc.setFill(WAYPOINT_COLOR);
		}
	}

	private void drawEndpoints(GraphicsContext gc, List<Vector2D> route) {
		if (route.isEmpty()) return;

		Vector2D start = toWorldPosition(route.get(0).add(new Vector2D(0.5f, 0.5f)));
		Vector2D end = toWorldPosition(route.get(route.size() - 1).add(new Vector2D(0.5f, 0.5f)));

		// Start point
		gc.setFill(START_COLOR);
		gc.fillOval(start.x() - 5, start.y() - 5, 10, 10);

		// End point
		gc.setFill(GOAL_COLOR);
		gc.fillOval(end.x() - 5, end.y() - 5, 10, 10);

		// Labels
		gc.setFill(LABEL_COLOR);
		gc.fillText("START", start.x() + 10, start.y() - 5);
		gc.fillText("GOAL", end.x() + 10, end.y() - 5);
	}

	private void drawCurrentWaypoint(GraphicsContext gc, List<Vector2D> route, PathfindingNode node) {
		int currentIndex = node.pathfindingComponent.getCurrentPathIndex();

		if (currentIndex >= 0 && currentIndex < route.size()) {
			Vector2D pos = toWorldPosition(route.get(currentIndex).add(new Vector2D(0.5f, 0.5f)));

			// Draw highlight around current waypoint
			gc.setStroke(CURRENT_WAYPOINT_COLOR);
			gc.setLineWidth(2);
			gc.strokeOval(pos.x() - 7, pos.y() - 7, 14, 14);

			// Draw label
			gc.setFill(CURRENT_WAYPOINT_COLOR);
			gc.fillText("CURRENT", pos.x() + 10, pos.y() + 15);
		}
	}

	private void drawDirectionalArrows(GraphicsContext gc, List<Vector2D> route) {
		if (route.size() < 2) return;

		gc.setStroke(ARROW_COLOR);
		gc.setLineWidth(1.5);

		for (int i = 0; i < route.size() - 1; i++) {
			Vector2D current = toWorldPosition(route.get(i).add(new Vector2D(0.5f, 0.5f)));
			Vector2D next = toWorldPosition(route.get(i + 1).add(new Vector2D(0.5f, 0.5f)));

			// Calculate direction vector
			Vector2D direction = next.subtract(current);

			// Skip very short segments
			if (direction.magnitudeSquared() < 100) continue;

			// Normalize and scale to arrow length
			direction = direction.normalize().scale(10);

			// Calculate midpoint of segment for arrow placement
			Vector2D midpoint = current.add(next).scale(0.5f);

			// Draw arrow line
			gc.strokeLine(midpoint.x(), midpoint.y(),
				midpoint.x() + direction.x(), midpoint.y() + direction.y());

			// Draw arrowhead
			double arrowSize = 4;
			Vector2D dir = direction.normalize();
			Vector2D perpendicular = new Vector2D(-dir.y(), dir.x());

			Vector2D arrowTip = midpoint.add(direction);
			Vector2D arrowLeft = arrowTip.subtract(dir.scale((float)arrowSize))
				.add(perpendicular.scale((float)arrowSize * 0.5f));
			Vector2D arrowRight = arrowTip.subtract(dir.scale((float)arrowSize))
				.subtract(perpendicular.scale((float)arrowSize * 0.5f));

			gc.strokeLine(arrowTip.x(), arrowTip.y(), arrowLeft.x(), arrowLeft.y());
			gc.strokeLine(arrowTip.x(), arrowTip.y(), arrowRight.x(), arrowRight.y());
		}
	}

	private Vector2D toWorldPosition(Vector2D position) {
		return position.scale((float) TILE_SIZE);
	}
}