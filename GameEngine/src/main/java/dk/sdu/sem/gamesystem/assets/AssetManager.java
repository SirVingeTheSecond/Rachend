package dk.sdu.sem.gamesystem.assets;

import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles all asset management.
 */
public class AssetManager {
	// I think this should just remain a singleton
	private static final AssetManager instance = new AssetManager();

	// Also made a small wrapper for assets with reference counting
	private static class AssetEntry {
		final Object asset;
		int refCount;

		AssetEntry(Object asset) {
			this.asset = asset;
			this.refCount = 1;
		}
	}

	// Maps asset IDs to the actual assets
	private final Map<String, AssetEntry> assetRegistry = new ConcurrentHashMap<>();

	// Maps asset types to their loaders
	private final Map<Class<?>, IAssetLoader<?>> assetLoaders = new HashMap<>();

	// Maps asset ID's to their descriptors
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
			AssetEntry entry = assetRegistry.get(assetId);
			entry.refCount++;
			return (T) entry.asset;
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
		assetRegistry.put(assetId, new AssetEntry(asset));
		return asset;
	}

	/**
	 * Pre-loads an asset by its ID
	 */
	@SuppressWarnings("unchecked")
	public <T> T preloadAsset(String assetId) {
		// Skip if already loaded
		if (assetRegistry.containsKey(assetId)) {
			AssetEntry entry = assetRegistry.get(assetId);
			entry.refCount++;
			return (T) entry.asset;
		}

		// Get descriptor
		AssetDescriptor<T> descriptor = (AssetDescriptor<T>) assetDescriptors.get(assetId);
		if (descriptor == null) {
			throw new IllegalArgumentException("No asset descriptor found for: " + assetId);
		}

		// Load asset
		IAssetLoader<T> loader = (IAssetLoader<T>) assetLoaders.get(descriptor.getAssetType());
		T asset = loader.loadAsset(descriptor);
		assetRegistry.put(assetId, new AssetEntry(asset));
		return asset;
	}

	/**
	 * Releases a reference to an asset. When the reference count reaches zero,
	 * the asset is unloaded.
	 */
	public boolean releaseAsset(String assetId) {
		AssetEntry entry = assetRegistry.get(assetId);
		if (entry == null) {
			return false;
		}

		entry.refCount--;
		if (entry.refCount <= 0) {
			unloadAsset(assetId);
			return true;
		}
		return false;
	}

	/**
	 * Unloads an asset by its ID and disposes of any resources
	 */
	public void unloadAsset(String assetId) {
		AssetEntry entry = assetRegistry.remove(assetId);
		if (entry != null) {
			disposeAsset(entry.asset);
		}
	}

	/**
	 * Disposes of resources held by an asset
	 */
	private void disposeAsset(Object asset) {
		if (asset instanceof Image) {
			System.gc();
		}
	}

	/**
	 * Unloads all assets that aren't being referenced
	 */
	public void unloadUnusedAssets() {
		// I create a list to avoid possible concurrency problems
		assetRegistry.keySet().stream()
			.filter(id -> assetRegistry.get(id).refCount <= 0)
			.toList()
			.forEach(this::unloadAsset);
	}
}