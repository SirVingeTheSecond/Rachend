package dk.sdu.sem.roomsystem;

import dk.sdu.sem.commonlevel.room.IRoomCreatedListener;
import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonlevel.room.Zone;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.enemy.IEnemyFactory;

import java.util.List;
import java.util.ServiceLoader;

public class RoomHelper implements IRoomCreatedListener {

	@Override
	public void onRoomCreated(Room room) {
		spawnEnemies(room);
	}

	private void spawnEnemies(Room room) {
		IEnemyFactory enemyFactory = ServiceLoader.load(IEnemyFactory.class).findFirst().orElse(null);

		List<Vector2D> enemySpawns = room.getZonePositions(Zone.ENEMY_SPAWN_POINT);

		if (enemyFactory != null && !enemySpawns.isEmpty()) {
			for (int i = 0; i < 4; i++) {
				Vector2D point = enemySpawns.get((int) (Math.random() * enemySpawns.size()));

				Entity enemy = enemyFactory.create(point, 100, 5, 3);
				room.getScene().addEntity(enemy);
			}
		}
	}
}
