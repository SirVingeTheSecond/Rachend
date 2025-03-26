package dk.sdu.sem.gamesystem.assets.managers;

import dk.sdu.sem.gamesystem.assets.AssetDescriptor;
import dk.sdu.sem.gamesystem.assets.IDisposable;
import dk.sdu.sem.gamesystem.assets.loaders.IAssetLoader;
import dk.sdu.sem.gamesystem.assets.references.AssetReferenceFactory;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import javafx.application.Platform;
import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import dk.sdu.sem.gamesystem.rendering.SpriteMap;

/**
 * Manages all asset lifecycle operations in the infrastructure layer.
 * Provides a single source of truth for asset caching and reference counting.
 */
public class AssetManager {
	// Singleton instance
	private static final AssetManager instance = new AssetManager();

	// Asset entry is used for reference counting
	private static class AssetEntry {
		final Object asset;
		final Class<?> type;
		int refCount;
		long lastAccessTime;

		AssetEntry(Object asset) {
			this.asset = asset;
			this.type = asset.getClass();
			this.refCount = 1;
			this.lastAccessTime = System.currentTimeMillis();
		}

		void updateAccessTime() {
			this.lastAccessTime = System.currentTimeMillis();
		}
	}

	// Maps asset IDs to the actual assets
	private final Map<String, AssetEntry> assetRegistry = new ConcurrentHashMap<>();

	// Maps asset types to their loaders
	private final Map<Class<?>, IAssetLoader<?>> assetLoaders = new HashMap<>();

	// Maps asset ID's to their descriptors
	private final Map<String, AssetDescriptor<?>> assetDescriptors = new ConcurrentHashMap<>();

	// Track unused descriptors to clean them up
	private final Set<String> unusedDescriptors = new HashSet<>();

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
	 * Gets or loads an asset by its reference.
	 * Includes type safety checks to prevent ClassCastExceptions.
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

			// Type safety check to prevent ClassCastException
			if (!assetType.isInstance(entry.asset)) {
				throw new IllegalArgumentException(String.format(
					"Type mismatch: Asset '%s' is of type %s but requested as %s",
					assetId, entry.type.getName(), assetType.getName()
				));
			}

			entry.refCount++;
			entry.updateAccessTime();
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
		if (asset == null) {
			throw new IllegalStateException("Asset loader returned null for: " + assetId);
		}

		assetRegistry.put(assetId, new AssetEntry(asset));
		// Remove from unused descriptors if present
		unusedDescriptors.remove(assetId);
		return asset;
	}

	/**
	 * Directly stores an asset that's already been created.
	 * Includes type collision detection to prevent overwriting different types.
	 */
	public <T> void storeAsset(String assetId, T asset) {
		if (asset == null) {
			throw new IllegalArgumentException("Cannot store null asset");
		}

		// Check for type collision
		AssetEntry existing = assetRegistry.get(assetId);
		if (existing != null) {
			if (!existing.asset.getClass().equals(asset.getClass())) {
				throw new IllegalArgumentException(String.format(
					"Type collision: Cannot store asset '%s' of type %s because it already exists as type %s",
					assetId, asset.getClass().getName(), existing.type.getName()
				));
			}
			// Same type, just increase ref count
			existing.refCount++;
			existing.updateAccessTime();
		} else {
			// New asset
			assetRegistry.put(assetId, new AssetEntry(asset));
		}
	}

	/**
	 * Checks if an asset is already registered.
	 */
	public boolean hasAssetDescriptor(String assetId) {
		return assetDescriptors.containsKey(assetId);
	}

	/**
	 * Checks if an asset is already loaded in the registry.
	 */
	public boolean isAssetLoaded(String assetId) {
		return assetRegistry.containsKey(assetId);
	}

	/**
	 * Gets the type of loaded asset.
	 * Returns null if the asset is not loaded.
	 */
	public Class<?> getLoadedAssetType(String assetId) {
		AssetEntry entry = assetRegistry.get(assetId);
		return entry != null ? entry.type : null;
	}

	/**
	 * Loads an asset by simple name and type, without requiring a reference.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAssetByName(String name, Class<T> type) {
		// Create the appropriate reference type
		IAssetReference<T> reference = AssetReferenceFactory.createReference(name, type);
		return getAsset(reference);
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
			entry.updateAccessTime();
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
		if (asset == null) {
			throw new IllegalStateException("Asset loader returned null for: " + assetId);
		}

		assetRegistry.put(assetId, new AssetEntry(asset));
		// Remove from unused descriptors if present
		unusedDescriptors.remove(assetId);
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
			// Mark the descriptor as unused
			unusedDescriptors.add(assetId);
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
		// Properly dispose of different asset types using IDisposable where applicable
		if (asset instanceof IDisposable) {
			((IDisposable) asset).dispose();
		} else if (asset instanceof Image) {
			// It is on purpose the JavaFX Image is not marked as IDisposable
			// JavaFX resources need to be disposed on the JavaFX thread
			Platform.runLater(() -> {
				try {
					// Cancel any ongoing loading and allow image to be garbage collected
					((Image)asset).cancel();
				} catch (Exception e) {
					System.err.println("Error disposing image: " + e.getMessage());
				}
			});
		}

		// Suggest garbage collection after disposing large assets
		if (asset instanceof Image || asset instanceof SpriteMap) {
			System.gc();
		}
	}

	/**
	 * Unloads all assets that aren't being referenced
	 */
	public void unloadUnusedAssets() {
		// Create a list to avoid possible concurrency problems
		assetRegistry.keySet().stream()
			.filter(id -> assetRegistry.get(id).refCount <= 0)
			.toList()
			.forEach(this::unloadAsset);

		cleanupUnusedDescriptors();
	}

	/**
	 * Removes descriptors for assets that have been unloaded and
	 * are no longer needed by any scene
	 */
	public void cleanupUnusedDescriptors() {
		// Only remove descriptors that have been marked as unused
		for (String id : unusedDescriptors) {
			assetDescriptors.remove(id);
		}
		unusedDescriptors.clear();
	}

	/**
	 * Returns the current number of loaded assets
	 */
	public int getLoadedAssetCount() {
		return assetRegistry.size();
	}

	/**
	 * Returns the current number of asset descriptors
	 */
	public int getDescriptorCount() {
		return assetDescriptors.size();
	}

	/**
	 * Clear all loaded assets
	 */
	public void clear() {
		// Unload all assets first
		for (String assetId : new HashSet<>(assetRegistry.keySet())) {
			unloadAsset(assetId);
		}
		assetRegistry.clear();
	}
}