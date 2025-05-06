package dk.sdu.sem.pathfindingsystem;

import dk.sdu.sem.commonsystem.Vector2D;

public class PathNode {
	public Vector2D position;
	public PathNode parent;
	public float gCost; // Accumulated cost from the start node
	public float hCost; // Heuristic cost to the target
	public float fCost; // gCost + hCost

	public PathNode(Vector2D position, PathNode parent, Vector2D targetPosition, float obstaclePenalty) {
		this.position = position;
		this.parent = parent;

		// Base G cost is the accumulated path distance
		float baseGCost = (parent == null) ? 0 : parent.gCost + Vector2D.euclidean_distance(parent.position, position);

		// Add obstacle proximity penalty to encourage paths away from walls
		this.gCost = baseGCost + obstaclePenalty;

		// Heuristic cost is still direct distance to target
		this.hCost = Vector2D.euclidean_distance(position, targetPosition);

		// Total cost combines both
		this.fCost = this.gCost + this.hCost;
	}

	// Default constructor with no obstacle penalty
	public PathNode(Vector2D position, PathNode parent, Vector2D targetPosition) {
		this(position, parent, targetPosition, 0);
	}
}