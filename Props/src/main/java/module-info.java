import dk.sdu.sem.commonlevel.room.IRoomCreatedListener;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;

module Props {
	requires CommonWeapon;
	requires GameEngine;
	requires CommonCollision;
	requires CommonLevel;
	requires Common;

	provides IRoomCreatedListener with dk.sdu.sem.props.PropSpawner;
	provides IAssetProvider with dk.sdu.sem.props.PropAssetProvider;
}