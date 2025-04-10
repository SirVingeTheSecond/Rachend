package dk.sdu.sem.roomsystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.gamesystem.services.IStart;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.player.PlayerComponent;

public class LevelTest implements IUpdate, IStart {
	static Scene[][] level = new Scene[5][5];
	RoomManager generator = new RoomManager();

	static int[] currentLevel = new int[] {2,2};

	@Override
	public void update() {

		Entity player = Scene.getActiveScene().getEntitiesWithComponent(PlayerComponent.class).stream().findFirst().orElse(null);

		if (player != null) {
			TransformComponent transform = player.getComponent(TransformComponent.class);
			int x = 0;
			int y = 0;

			if (transform.getPosition().x() > GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x()) {
				x = 1;
				transform.setPosition(new Vector2D(20F, GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y() * 0.5f));
			} else if (transform.getPosition().x() < 0) {
				x = -1;
				transform.setPosition(new Vector2D(GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x() - 20, GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y() * 0.5f));
			} else if (transform.getPosition().y() > GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y()) {
				y = -1;
				transform.setPosition(new Vector2D(GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x() * 0.5f, 20));
			} else if (transform.getPosition().y() < 0) {
				y = 1;
				transform.setPosition(new Vector2D(GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x() * 0.5f, GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y() - 20));
			}

			if (x != 0) {
				currentLevel[0] += x;
				changeRoom();
			} else if (y != 0) {
				currentLevel[1] += y;
				changeRoom();
			}
		}
	}

	private void changeRoom() {
		if (level[currentLevel[0]][currentLevel[1]] != null) {
			SceneManager.getInstance().setActiveScene(level[currentLevel[0]][currentLevel[1]]);
		} else {
			boolean north = currentLevel[1] < level[1].length - 1;
			boolean south = currentLevel[1] > 0;
			boolean east = currentLevel[0] < level[0].length - 1;
			boolean west = currentLevel[0] > 0;

			Scene newScene = generator.createRoom(north,east,south,west);
			level[currentLevel[0]][currentLevel[1]] = newScene;
			SceneManager.getInstance().setActiveScene(newScene);
		}
	}

	@Override
	public void start() {
		Scene startingRoom = generator.createRoom(true,true,true,true);
		SceneManager.getInstance().setActiveScene(startingRoom);

		level[2][2] = startingRoom;
	}
}
