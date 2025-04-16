package dk.sdu.sem.levelsystem;

import dk.sdu.sem.commonlevel.ILevelSPI;
import dk.sdu.sem.commonlevel.IRoomSPI;
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
	private static HashMap<Integer, Scene> sceneMap = new HashMap<>();
	private static int currentRoom = -1;

	public LevelManager() {
		roomSPI = ServiceLoader.load(IRoomSPI.class).findFirst().orElse(null);
	}

	@Override
	public void generateLevel(int minRooms, int maxRooms) {
		Level level = new Level(minRooms, maxRooms);
		level.createLayout();
		boolean[][] layout = level.getLayout();

		for (int x = 0; x < layout.length; x++) {
			if (x == level.getStartRoom()) {
				Scene scene = roomSPI.createRoom("start.json", layout[x][1], layout[x][2], layout[x][3], layout[x][4]);
				sceneMap.put(x,scene);
				SceneManager.getInstance().setActiveScene(scene);
			}
			else if (layout[x][0]) {
				Scene scene = roomSPI.createRoom(layout[x][1], layout[x][2], layout[x][3], layout[x][4]);
				sceneMap.put(x,scene);
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
				transform.setPosition(new Vector2D(20F, GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y() * 0.5f));
				changeRoom();
			} else if (transform.getPosition().x() < 0) {
				currentRoom -= 1;
				transform.setPosition(new Vector2D(GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x() - 20, GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y() * 0.5f));
				changeRoom();
			} else if (transform.getPosition().y() > GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y()) {
				currentRoom += 10;
				transform.setPosition(new Vector2D(GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x() * 0.5f, 20));
				changeRoom();
			} else if (transform.getPosition().y() < 0) {
				currentRoom -= 10;
				transform.setPosition(new Vector2D(GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x() * 0.5f, GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y() - 20));
				changeRoom();
			}
		}
	}

	private void changeRoom() {
		SceneManager.getInstance().setActiveScene(sceneMap.get(currentRoom));
	}
}

