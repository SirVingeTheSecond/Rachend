module GameEngine {
	uses dk.sdu.sem.gamesystem.services.IProcessor;
	exports dk.sdu.sem.gamesystem.services;
	exports dk.sdu.sem.gamesystem.data;

	requires Common;
	requires javafx.graphics;
	exports dk.sdu.sem.gamesystem;
}