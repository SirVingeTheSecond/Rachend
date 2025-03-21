import dk.sdu.sem.gamesystem.services.IGUIUpdate;

module UI {
	requires GameEngine;
	requires javafx.graphics;
	requires java.desktop;
	requires Common;

	provides IGUIUpdate with dk.sdu.sem.uisystem.Crosshair;
}