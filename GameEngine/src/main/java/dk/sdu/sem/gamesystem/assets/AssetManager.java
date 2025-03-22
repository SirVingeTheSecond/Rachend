package dk.sdu.sem.gamesystem.assets;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles all asset management.
 */
public class AssetManager {
	// Still a singleton, but with improved modularity
	private static final AssetManager instance = new AssetManager();

	// Maps asset identifiers to the actual assets
	private final Map<String, Object> assetRegistry = new ConcurrentHashMap<>();

	// Maps asset types to their loaders
	private final Map<Class<?>, IAssetLoader<?>> assetLoaders = new HashMap<>();

	// Maps asset identifiers to their descriptors
	private final Map<String, AssetDescriptor<?>> assetDescriptors = new ConcurrentHashMap<>();

	private AssetManager() {
		// Load all asset providers from modules
		loadAssetLoaders();
	}

	public static AssetManager getInstance() {
		return instance;
	}

	/**
	 * Loads asset loaders using ServiceLoader
	 */
	private void loadAssetLoaders() {
		ServiceLoader.load(IAssetLoader.class).forEach(loader ->
			assetLoaders.put(loader.getAssetType(), loader));
	}

	/**
	 * Registers an asset descriptor without loading the asset.
	 * This allows assets to be defined in one module and loaded later.
	 */
	public <T> void registerAsset(AssetDescriptor<T> descriptor) {
		assetDescriptors.put(descriptor.getId(), descriptor);
	}

	/**
	 * Gets or loads an asset by its reference
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAsset(IAssetReference<T> reference) {
		String assetId = reference.getAssetId();
		Class<T> assetType = reference.getAssetType();

		System.out.println("Requesting asset: " + assetId + " of type " + assetType.getName());

		// Return asset if already loaded
		if (assetRegistry.containsKey(assetId)) {
			System.out.println("Found cached asset: " + assetId);
			return (T) assetRegistry.get(assetId);
		}

		// Find descriptor
		AssetDescriptor<T> descriptor = (AssetDescriptor<T>) assetDescriptors.get(assetId);
		if (descriptor == null) {
			System.out.println("Available asset descriptors: " + String.join(", ", assetDescriptors.keySet()));
			throw new IllegalArgumentException("No asset descriptor found for: " + assetId);
		}

		// Find loader for this asset type
		IAssetLoader<T> loader = (IAssetLoader<T>) assetLoaders.get(assetType);
		if (loader == null) {
			throw new IllegalArgumentException("No loader found for asset type: " + assetType.getName());
		}

		// Load asset
		T asset = loader.loadAsset(descriptor);
		assetRegistry.put(assetId, asset);
		return asset;
	}

	/**
	 * Pre-loads an asset by its ID
	 */
	@SuppressWarnings("unchecked")
	public <T> T preloadAsset(String assetId) {
		// Skip if already loaded
		if (assetRegistry.containsKey(assetId)) {
			return (T) assetRegistry.get(assetId);
		}

		// Get descriptor
		AssetDescriptor<T> descriptor = (AssetDescriptor<T>) assetDescriptors.get(assetId);
		if (descriptor == null) {
			throw new IllegalArgumentException("No asset descriptor found for: " + assetId);
		}

		// Load asset
		IAssetLoader<T> loader = (IAssetLoader<T>) assetLoaders.get(descriptor.getAssetType());
		T asset = loader.loadAsset(descriptor);
		assetRegistry.put(assetId, asset);
		return asset;
	}

	/**
	 * Unloads an asset by its ID
	 */
	public void unloadAsset(String assetId) {
		assetRegistry.remove(assetId);
	}
}