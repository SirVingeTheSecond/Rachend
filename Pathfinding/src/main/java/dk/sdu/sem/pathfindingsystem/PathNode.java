package dk.sdu.sem.pathfindingsystem;

import dk.sdu.sem.commonsystem.Vector2D;

public class PathNode {
	public Vector2D position;
	public PathNode parent;
	public float gCost; // Accumulated cost from the start node
	public float hCost; // Heuristic cost to the target
	public float fCost; // gCost + hCost

	public PathNode(Vector2D position, PathNode parent, Vector2D targetPosition) {
		this.position = position;
		this.parent = parent;
		this.gCost = (parent == null) ? 0 : parent.gCost + Vector2D.euclidean_distance(parent.position, position);
		this.hCost = Vector2D.euclidean_distance(position, targetPosition);
		this.fCost = this.gCost + this.hCost;
	}
}