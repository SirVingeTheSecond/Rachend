package dk.sdu.sem.props;

import dk.sdu.sem.collision.shapes.Bounds;
import dk.sdu.sem.commonlevel.room.IRoomCreatedListener;
import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonlevel.room.ZoneType;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.gamesystem.GameConstants;

import java.util.List;

public class PropSpawner implements IRoomCreatedListener {
	@Override
	public void onRoomCreated(Room room) {

		List<Room.Zone> zones = room.getZones(ZoneType.PROPS);
		for (Room.Zone zone : zones) {
			if (zone.getWidth() == 0 && zone.getHeight() == 0) {
				//Point
				Bounds b = Bounds.fromCenter(
					zone.getPosition(),
					GameConstants.TILE_SIZE / 2f,
					GameConstants.TILE_SIZE / 2f
				);

				for (Entity prop : PropFactory.createProps(b, 5)) {
					room.getScene().addEntity(prop);
				}
			}
			else {
				Bounds b = new Bounds(
					zone.getPosition().x(), zone.getPosition().y(),
					zone.getWidth(), zone.getHeight()
				);

				int props = Math.round(((b.getWidth() / GameConstants.TILE_SIZE) * (b.getHeight() / GameConstants.TILE_SIZE)) / 2f);

				for (Entity prop : PropFactory.createProps(b, props)) {
					room.getScene().addEntity(prop);
				}
			}
		}

		/*
		List<Vector2D> propPoints = room.getZonePositions(ZoneType.PROP);
		for (Vector2D propPoint : propPoints) {
			Bounds b = Bounds.fromCenter(
					propPoint,
					GameConstants.TILE_SIZE / 2f,
					GameConstants.TILE_SIZE / 2f
			);

			for (Entity prop : PropFactory.createProps(b, 5)) {
				room.getScene().addEntity(prop);
			}
		}*/
	}
}
