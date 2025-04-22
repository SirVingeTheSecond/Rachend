package dk.sdu.sem.props;

import dk.sdu.sem.collision.shapes.Bounds;
import dk.sdu.sem.commonlevel.room.IRoomCreatedListener;
import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonlevel.room.Zone;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;

import java.util.List;

public class PropSpawner implements IRoomCreatedListener {
	@Override
	public void onRoomCreated(Room room) {

		List<Vector2D> propPoints = room.getZonePositions(Zone.PROP_SPAWN_POINT);
		for (Vector2D propPoint : propPoints) {
			Bounds b = Bounds.fromCenter(
					propPoint,
					GameConstants.TILE_SIZE / 2f,
					GameConstants.TILE_SIZE / 2f
			);

			for (Entity prop : PropFactory.createProps(b)) {
				room.getScene().addEntity(prop);
			}
		}
	}
}
