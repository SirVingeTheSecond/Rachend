import dk.sdu.sem.commonlevel.room.IRoomCreatedListener;
import dk.sdu.sem.commonsystem.INodeProvider;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
import dk.sdu.sem.gamesystem.services.IUpdate;

module Props {
	requires CommonWeapon;
	requires CommonStats;
	requires GameEngine;
	requires CommonCollision;
	requires CommonLevel;
	requires Common;

	provides IRoomCreatedListener with dk.sdu.sem.props.PropSpawner;
	provides IAssetProvider with dk.sdu.sem.props.PropAssetProvider;
	provides IUpdate with dk.sdu.sem.props.PropSystem;
	provides INodeProvider with dk.sdu.sem.props.BreakableNode;
	provides Node with dk.sdu.sem.props.BreakableNode;
}