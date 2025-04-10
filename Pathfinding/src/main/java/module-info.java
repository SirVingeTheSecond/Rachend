module Pathfinding {
	exports dk.sdu.sem.pathfindingsystem;
	requires GameEngine;
	requires CommonPlayer;
	requires CommonCollision;
	requires Common;
    requires javafx.graphics;

    provides dk.sdu.sem.gamesystem.services.IUpdate with
		dk.sdu.sem.pathfindingsystem.PathfindingSystem;

	provides dk.sdu.sem.commonsystem.Node with
		dk.sdu.sem.pathfindingsystem.PathfindingNode;

	provides dk.sdu.sem.commonsystem.INodeProvider with
		dk.sdu.sem.pathfindingsystem.PathfindingNode;

	provides dk.sdu.sem.gamesystem.services.IGUIUpdate with
		dk.sdu.sem.pathfindingsystem.PathfindingSystem;

}