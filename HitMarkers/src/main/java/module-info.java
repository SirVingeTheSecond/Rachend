import dk.sdu.sem.commonsystem.INodeProvider;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;

module HitMarkers {
	requires CommonStats;
	requires GameEngine;
	requires Common;
	requires javafx.graphics;

	provides Node with dk.sdu.sem.hitmarkers.HitMarkerNode;
	provides INodeProvider with dk.sdu.sem.hitmarkers.HitMarkerNode;
}