package dk.sdu.sem.gamesystem;
import dk.sdu.sem.gamesystem.data.Entity;
import dk.sdu.sem.gamesystem.data.Scene;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SceneManager {
	private static final SceneManager instance = new SceneManager();

	private Scene activeScene;
	private final HashMap<String, Scene> scenes = new HashMap<String, Scene>();

	private final Set<Entity> persistedEntities = new HashSet<Entity>();

	private SceneManager() {}

	public static SceneManager getInstance() {
		return instance;
	}

	/**
	 * @return Currently active scene
	 */
	public Scene getActiveScene() {
		return activeScene;
	}


	/**
	 * Find and get a scene by name
	 * Returns null if scene not found
	 */
	public Scene findScene(String sceneName) {
		return scenes.getOrDefault(sceneName, null);
	}

	/**
	 * Makes Scene active and adds it to Managers list of scenes.
	 * @param scene Scene to set active
	 */
	public void setActiveScene(Scene scene) {
		addScene(scene);

		transferPersistedEntities(scene);

		this.activeScene = scene;
	}

	/**
	 * Adds scene to Managers list of scenes.
	 */
	public void addScene(Scene scene) {
		scenes.put(scene.getName(), scene);
	}

	public void addPersistedEntity(Entity entity) {
		persistedEntities.add(entity);
	}

	public void removePersistedEntity(Entity entity) {
		persistedEntities.remove(entity);
	}

	private void transferPersistedEntities(Scene newScene) {
		for (Entity entity : persistedEntities) {
			activeScene.removeEntity(entity);
			newScene.addEntity(entity);
		}

		//Transfer nodes as well
	}

}
