import dk.sdu.sem.commonsystem.debug.IColliderRenderer;
import dk.sdu.sem.commonsystem.debug.IPathfindingRenderer;
import dk.sdu.sem.commonsystem.debug.IRaycastRenderer;

module DebugRenderer {
	requires Common;
	requires GameEngine;
	requires javafx.graphics;

	uses IColliderRenderer;
	uses IPathfindingRenderer;
	uses IRaycastRenderer;
	uses dk.sdu.sem.commonsystem.debug.IDebugStateChangeListener;

	provides dk.sdu.sem.commonsystem.debug.IDebugController with
		dk.sdu.sem.debugrenderer.DebugController;

	provides dk.sdu.sem.commonsystem.debug.IDebugDrawManager with
		dk.sdu.sem.debugrenderer.DebugDrawManager;

	provides dk.sdu.sem.gamesystem.services.IGUIUpdate with
		dk.sdu.sem.debugrenderer.DebugRenderer;

	exports dk.sdu.sem.debugrenderer;
}