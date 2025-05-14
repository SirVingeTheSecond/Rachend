import dk.sdu.sem.commonsystem.debug.IColliderRenderer;
import dk.sdu.sem.commonsystem.debug.IPathfindingRenderer;

module Common {
	requires javafx.graphics;

	exports dk.sdu.sem.commonsystem;
	exports dk.sdu.sem.logging;
	exports dk.sdu.sem.commonsystem.ui;
	exports dk.sdu.sem.commonsystem.debug;

	uses dk.sdu.sem.commonsystem.Node;
	uses dk.sdu.sem.commonsystem.INodeProvider;
	uses dk.sdu.sem.commonsystem.IEntityLifecycleListener;
	uses dk.sdu.sem.commonsystem.debug.IDebugDrawManager;
	uses IColliderRenderer;
	uses IPathfindingRenderer;
	uses dk.sdu.sem.commonsystem.debug.IDebugStateChangeListener;
}