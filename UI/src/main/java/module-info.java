import dk.sdu.sem.commonsystem.INodeProvider;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import dk.sdu.sem.gamesystem.services.IStart;

module UI {
	requires GameEngine;
	requires javafx.graphics;
	requires java.desktop;
	requires CommonHealth;
	requires CommonStats;
	requires CommonPlayer;
	requires Common;

	provides IGUIUpdate with
		dk.sdu.sem.uisystem.Crosshair,
			dk.sdu.sem.uisystem.HealthBar;

	provides Node with dk.sdu.sem.uisystem.HealthBarNode;

	provides INodeProvider with dk.sdu.sem.uisystem.HealthBarNode;

	provides IStart with dk.sdu.sem.uisystem.HealthBar;

	provides IAssetProvider with dk.sdu.sem.uisystem.HealthBar;

	exports dk.sdu.sem.uisystem;
}