package dk.sdu.sem.levelsystem;

import dk.sdu.sem.commonlevel.ILevelSPI;
import dk.sdu.sem.commonlevel.IRoomSPI;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.gamesystem.scenes.SceneManager;

import java.util.HashMap;
import java.util.ServiceLoader;

public class LevelManager  implements ILevelSPI {
	IRoomSPI roomSPI;
	HashMap<Integer, Scene> sceneMap = new HashMap<>();

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
	}
}

