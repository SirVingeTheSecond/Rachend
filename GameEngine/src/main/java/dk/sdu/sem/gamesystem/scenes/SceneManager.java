package dk.sdu.sem.gamesystem.scenes;

import dk.sdu.sem.gamesystem.data.Entity;
import dk.sdu.sem.gamesystem.data.Scene;

import java.util.HashMap;
import java.util.Set;

public class SceneManager {
	private static final SceneManager instance = new SceneManager();

	private Scene activeScene = new Scene("Main");
	private final HashMap<String, Scene> scenes = new HashMap<>();

	private SceneManager() {
		scenes.put(activeScene.getName(), activeScene);
	}

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
		activeScene.addPersistedEntity(entity);
	}

	public void removePersistedEntity(Entity entity) {
		activeScene.removePersistedEntity(entity);
	}

	private void transferPersistedEntities(Scene newScene) {
		if (activeScene == null)
			return;

		// Persisted entities from the active scene
		Set<Entity> persistedEntities = activeScene.getPersistedEntities();

		// Add persisted entities to the new scene
		for (Entity entity : persistedEntities) {
			activeScene.removeEntity(entity);

			// Add to new scene (will process the entity through the NodeManager of the new scene)
			newScene.addEntity(entity);

			// We mark it as persisted in the new scene
			newScene.addPersistedEntity(entity);
		}
	}
}