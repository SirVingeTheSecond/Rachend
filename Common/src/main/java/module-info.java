module Common {
	requires javafx.graphics;

	exports dk.sdu.sem.commonsystem;
	exports dk.sdu.sem.logging;
	exports dk.sdu.sem.commonsystem.ui;
	exports dk.sdu.sem.commonsystem.debug;
	exports dk.sdu.sem.commonsystem.events;

	uses dk.sdu.sem.commonsystem.Node;
	uses dk.sdu.sem.commonsystem.INodeProvider;
	uses dk.sdu.sem.commonsystem.IEntityLifecycleListener;
	uses dk.sdu.sem.commonsystem.debug.IDebugDrawManager;
	uses dk.sdu.sem.commonsystem.debug.IDebugController;
	uses dk.sdu.sem.commonsystem.debug.IColliderRenderer;
	uses dk.sdu.sem.commonsystem.debug.IPathfindingRenderer;
	uses dk.sdu.sem.commonsystem.debug.IDebugStateChangeListener;
}