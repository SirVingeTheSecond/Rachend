import dk.sdu.sem.commonsystem.INodeProvider;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.commonsystem.ui.IMenuSPI;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import dk.sdu.sem.gamesystem.services.IStart;

module UI {
	requires GameEngine;
	requires java.desktop;
	requires CommonHealth;
	requires CommonStats;
	requires CommonPlayer;
	requires Common;
	requires javafx.controls;

	provides IGUIUpdate with
		dk.sdu.sem.uisystem.Crosshair,
		dk.sdu.sem.uisystem.HealthBar,
		dk.sdu.sem.uisystem.StatsUI;

	provides Node with dk.sdu.sem.uisystem.HealthBarNode;

	provides INodeProvider with dk.sdu.sem.uisystem.HealthBarNode;

	provides IStart with dk.sdu.sem.uisystem.HealthBar;

	provides IAssetProvider with dk.sdu.sem.uisystem.HealthBar;

	provides IMenuSPI with dk.sdu.sem.uisystem.MenuManager;

	exports dk.sdu.sem.uisystem;
}