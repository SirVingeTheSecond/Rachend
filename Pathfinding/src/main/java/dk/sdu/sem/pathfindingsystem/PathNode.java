package dk.sdu.sem.pathfindingsystem;

import dk.sdu.sem.commonsystem.Vector2D;

public class PathNode {
	public Vector2D position;
	public PathNode parent;
	public int gCost; // Accumulated cost from the start node
	public int hCost; // Heuristic cost to the target
	public int fCost; // gCost + hCost

	public PathNode(Vector2D position, PathNode parent, Vector2D targetPosition) {
		this.position = position;
		this.parent = parent;
		this.gCost = (parent == null) ? 0 : parent.gCost + (int) Vector2D.euclidean_distance(parent.position, position);
		this.hCost = (int) Vector2D.euclidean_distance(position, targetPosition);
		this.fCost = this.gCost + this.hCost;
	}
}