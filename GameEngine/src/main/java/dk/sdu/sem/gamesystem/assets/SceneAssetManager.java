package dk.sdu.sem.gamesystem.assets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages assets tied to specific scenes.
 */
public class SceneAssetManager {
	private static final SceneAssetManager instance = new SceneAssetManager();
	private final AssetManager assetManager;

	// Maps scene names to their associated assets
	private final Map<String, Set<String>> sceneAssets = new HashMap<>();

	private SceneAssetManager() {
		assetManager = AssetManager.getInstance();
	}

	public static SceneAssetManager getInstance() {
		return instance;
	}

	/**
	 * Registers assets to be associated with a scene.
	 */
	public void registerSceneAssets(String sceneName, Set<String> assetIds) {
		sceneAssets.computeIfAbsent(sceneName, k -> new HashSet<>())
			.addAll(assetIds);
	}

	/**
	 * Preloads all assets for a scene.
	 */
	public void loadSceneAssets(String sceneName) {
		Set<String> assets = sceneAssets.get(sceneName);
		if (assets != null) {
			for (String assetId : assets) {
				assetManager.preloadAsset(assetId);
			}
		}
	}

	/**
	 * Releases all assets for a scene.
	 */
	public void unloadSceneAssets(String sceneName) {
		Set<String> assets = sceneAssets.get(sceneName);
		if (assets != null) {
			for (String assetId : assets) {
				assetManager.releaseAsset(assetId);
			}
		}
	}

	/**
	 * Updates asset management when the active scene changes.
	 */
	public void onSceneChanged(String oldSceneName, String newSceneName) {
		if (oldSceneName != null) {
			unloadSceneAssets(oldSceneName);
		}
		loadSceneAssets(newSceneName);
	}
}