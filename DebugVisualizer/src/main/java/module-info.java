module DebugVisualizer {
	requires Common;
	requires GameEngine;
	requires javafx.graphics;

	uses dk.sdu.sem.commonsystem.debug.IColliderVisualizer;
	uses dk.sdu.sem.commonsystem.debug.IDebugDrawManager;
	uses dk.sdu.sem.commonsystem.debug.IDebugStateChangeListener;

	provides dk.sdu.sem.commonsystem.debug.IDebugVisualizationSPI with
		dk.sdu.sem.debugvisualizer.DebugVisualizationService;

	provides dk.sdu.sem.commonsystem.debug.IDebugDrawManager with
		dk.sdu.sem.debugvisualizer.DebugDrawManager;

	provides dk.sdu.sem.gamesystem.services.IGUIUpdate with
		dk.sdu.sem.debugvisualizer.DebugRenderer;
}