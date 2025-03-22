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

	// Track currently loaded scenes for cleanup
	private final Set<String> loadedScenes = new HashSet<>();

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
				try {
					assetManager.preloadAsset(assetId);
				} catch (Exception e) {
					System.err.println("Error loading asset: " + assetId + " - " + e.getMessage());
				}
			}
			loadedScenes.add(sceneName);
		}
	}

	/**
	 * Releases all assets for a scene.
	 */
	public void unloadSceneAssets(String sceneName) {
		Set<String> assets = sceneAssets.get(sceneName);
		if (assets != null) {
			for (String assetId : assets) {
				try {
					assetManager.releaseAsset(assetId);
				} catch (Exception e) {
					System.err.println("Error unloading asset: " + assetId + " - " + e.getMessage());
				}
			}
			loadedScenes.remove(sceneName);
		}
	}

	/**
	 * Updates asset management when the active scene changes.
	 */
	public void onSceneChanged(String oldSceneName, String newSceneName) {
		if (oldSceneName != null) {
			unloadSceneAssets(oldSceneName);
		}
		if (newSceneName != null) {
			loadSceneAssets(newSceneName);
		}

		// Clean up unused assets after scene change
		assetManager.unloadUnusedAssets();
	}

	/**
	 * Register a single asset for a scene
	 */
	public void registerSceneAsset(String sceneName, String assetId) {
		Set<String> assets = sceneAssets.computeIfAbsent(sceneName, k -> new HashSet<>());
		assets.add(assetId);

		// If scene is already loaded, preload this asset too
		if (loadedScenes.contains(sceneName)) {
			try {
				assetManager.preloadAsset(assetId);
			} catch (Exception e) {
				System.err.println("Error loading asset: " + assetId + " - " + e.getMessage());
			}
		}
	}

	/**
	 * Unregister a single asset from a scene
	 */
	public void unregisterSceneAsset(String sceneName, String assetId) {
		Set<String> assets = sceneAssets.get(sceneName);
		if (assets != null) {
			assets.remove(assetId);

			// If scene is loaded, release this asset
			if (loadedScenes.contains(sceneName)) {
				try {
					assetManager.releaseAsset(assetId);
				} catch (Exception e) {
					System.err.println("Error unloading asset: " + assetId + " - " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Cleans up all assets and registrations
	 */
	public void clear() {
		// Unload assets for all loaded scenes
		for (String sceneName : new HashSet<>(loadedScenes)) {
			unloadSceneAssets(sceneName);
		}

		// Clear all registrations
		sceneAssets.clear();
		loadedScenes.clear();

		// Clean up unused assets
		assetManager.unloadUnusedAssets();
	}

	/**
	 * Returns the number of currently loaded scenes
	 */
	public int getLoadedSceneCount() {
		return loadedScenes.size();
	}

	/**
	 * Returns the total number of registered scene assets
	 */
	public int getTotalRegisteredAssetCount() {
		return sceneAssets.values().stream()
			.mapToInt(Set::size)
			.sum();
	}
}