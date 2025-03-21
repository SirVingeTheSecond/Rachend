package dk.sdu.sem.gamesystem.assets.registry;

import dk.sdu.sem.gamesystem.assets.AssetManager;

import java.util.List;
import java.util.ServiceLoader;

/**
 * Loads all asset providers using ServiceLoader and registers their assets.
 */
public class AssetRegistry {
	private final AssetManager assetManager;
	private final AssetRegistrar registrar;

	public AssetRegistry() {
		this.assetManager = AssetManager.getInstance();
		this.registrar = new AssetRegistrar(assetManager);
	}

	/**
	 * Initializes the asset registry by loading all providers.
	 */
	public void initialize() {
		ServiceLoader.load(IAssetRegistryProvider.class).forEach(provider -> {
			System.out.println("Registering assets from: " + provider.getClass().getName());
			provider.registerAssets(registrar);
		});
	}

	/**
	 * Preloads a set of assets by ID
	 */
	public void preloadAssets(List<String> assetIds) {
		for (String id : assetIds) {
			assetManager.preloadAsset(id);
		}
	}
}
