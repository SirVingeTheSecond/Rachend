package dk.sdu.sem.levelsystem;

import dk.sdu.sem.commonlevel.ILevelSPI;
import dk.sdu.sem.commonlevel.IRoomSPI;
import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonlevel.room.RoomType;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.player.PlayerComponent;

import java.util.HashMap;
import java.util.ServiceLoader;

public class LevelManager  implements ILevelSPI, IUpdate {
	private static IRoomSPI roomSPI;
	private static HashMap<Integer, Room> roomMap = new HashMap<>();
	private static int currentRoom = -1;

	public LevelManager() {
		roomSPI = ServiceLoader.load(IRoomSPI.class).findFirst().orElse(null);
	}

	@Override
	public void generateLevel(int minRooms, int maxRooms) {
		Level level = new Level(minRooms, maxRooms);
		level.createLayout();
		boolean[][] layout = level.getLayout();

		int boosRoom = level.getEndRooms().get((int) (Math.random() * level.getEndRooms().size()));

		for (int x = 0; x < layout.length; x++) {
			if (x == level.getStartRoom()) {
				Room room = roomSPI.createRoom(RoomType.START, layout[x][1], layout[x][2], layout[x][3], layout[x][4]);
				roomMap.put(x,room);
				SceneManager.getInstance().setActiveScene(room.getScene());
			}
			else if (x == boosRoom) {
				Room room = roomSPI.createRoom(RoomType.BOSS, layout[x][1], layout[x][2], layout[x][3], layout[x][4]);
				roomMap.put(x,room);
			}
			else if (layout[x][0]) {
				Room room = roomSPI.createRoom(RoomType.NORMAL, layout[x][1], layout[x][2], layout[x][3], layout[x][4]);
				roomMap.put(x,room);
			}
		}

		currentRoom = level.getStartRoom();
	}

	@Override
	public void update() {
		Entity player = Scene.getActiveScene().getEntitiesWithComponent(PlayerComponent.class).stream().findFirst().orElse(null);

		if (player != null) {
			TransformComponent transform = player.getComponent(TransformComponent.class);

			if (transform.getPosition().x() > GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x()) {
				currentRoom += 1;
				Vector2D pos = roomMap.get(currentRoom).getEntrances()[3];
				if (pos == null)
					pos = new Vector2D(20F, GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y() * 0.5f);
				transform.setPosition(pos);

				changeRoom();
			} else if (transform.getPosition().x() < 0) {
				currentRoom -= 1;
				Vector2D pos = roomMap.get(currentRoom).getEntrances()[1];
				if (pos == null)
					pos = new Vector2D(GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x() - 20, GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y() * 0.5f);
				transform.setPosition(pos);

				changeRoom();
			} else if (transform.getPosition().y() > GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y()) {
				currentRoom += 10;
				Vector2D pos = roomMap.get(currentRoom).getEntrances()[0];
				if (pos == null)
					pos = new Vector2D(GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x() * 0.5f, 20);
				transform.setPosition(pos);

				changeRoom();
			} else if (transform.getPosition().y() < 0) {
				currentRoom -= 10;
				Vector2D pos = roomMap.get(currentRoom).getEntrances()[2];
				if (pos == null)
					pos = new Vector2D(GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x() * 0.5f, GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y() - 20);
				transform.setPosition(pos);

				changeRoom();
			}
		}
	}

	private void changeRoom() {
		SceneManager.getInstance().setActiveScene(roomMap.get(currentRoom).getScene());
	}
}

