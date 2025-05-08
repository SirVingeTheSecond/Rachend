module Common {
	requires javafx.graphics;
	exports dk.sdu.sem.commonsystem;
	exports dk.sdu.sem.logging;
	exports dk.sdu.sem.commonsystem.ui;
	exports dk.sdu.sem.commonsystem.events;

	uses dk.sdu.sem.commonsystem.Node;
	uses dk.sdu.sem.commonsystem.INodeProvider;
}