import dk.sdu.sem.commonsystem.INodeProvider;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import dk.sdu.sem.gamesystem.services.IUpdate;

module Weapon {
	exports dk.sdu.sem.weaponsystem;
	requires CommonPlayer;
	requires GameEngine;
	requires Common;
	requires javafx.graphics;

	provides INodeProvider with dk.sdu.sem.weaponsystem.WeaponPlayerNode, dk.sdu.sem.weaponsystem.BulletNode, dk.sdu.sem.weaponsystem.WeaponNode;
	provides IUpdate with dk.sdu.sem.weaponsystem.WeaponPlayerSystem, dk.sdu.sem.weaponsystem.BulletSystem, dk.sdu.sem.weaponsystem.WeaponSystem;
	provides IGUIUpdate with dk.sdu.sem.weaponsystem.BulletSystem;
	provides Node with dk.sdu.sem.weaponsystem.WeaponPlayerNode, dk.sdu.sem.weaponsystem.BulletNode, dk.sdu.sem.weaponsystem.WeaponNode;
}