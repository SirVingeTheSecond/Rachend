package dk.sdu.sem.gamesystem.assets.managers;

import dk.sdu.sem.gamesystem.assets.AssetDescriptor;
import dk.sdu.sem.gamesystem.assets.IDisposable;
import dk.sdu.sem.gamesystem.assets.loaders.IAssetLoader;
import dk.sdu.sem.gamesystem.assets.references.AssetReferenceFactory;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.assets.references.SpriteMapTileReference;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
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
	private static final Logging LOGGER = Logging.createLogger("AssetManager", LoggingLevel.DEBUG);

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

		// Return asset if already loaded
		if (assetRegistry.containsKey(assetId)) {
			AssetEntry entry = assetRegistry.get(assetId);

			// Type safety check to prevent ClassCastException
			if (!assetType.isInstance(entry.asset)) {
				throw new AssetTypeException(assetId, assetType, entry.type);
			}

			entry.refCount++;
			entry.updateAccessTime();
			return (T) entry.asset;
		}

		// Find descriptor
		AssetDescriptor<T> descriptor = (AssetDescriptor<T>) assetDescriptors.get(assetId);
		if (descriptor == null) {
			LOGGER.error("Available asset descriptors: " + String.join(", ", assetDescriptors.keySet()));
			throw new AssetNotFoundException(assetId, assetType);
		}

		// Find loader for this asset type
		IAssetLoader<T> loader = (IAssetLoader<T>) assetLoaders.get(assetType);
		if (loader == null) {
			throw new AssetLoaderNotFoundException(assetType);
		}

		// Load asset
		T asset = loader.loadAsset(descriptor);
		if (asset == null) {
			throw new AssetLoadException(assetId);
		}

		assetRegistry.put(assetId, new AssetEntry(asset));
		// Remove from unused descriptors if present
		unusedDescriptors.remove(assetId);
		return asset;
	}

	/**
	 * Gets a sprite by its reference.
	 * Handles special resolution of sprite map tile references.
	 *
	 * @param reference The sprite reference
	 * @return The resolved sprite
	 */
	public Sprite resolveSprite(IAssetReference<Sprite> reference) {
		if (reference == null) {
			return null;
		}

		// Special handling for sprite map tile references
		if (reference instanceof SpriteMapTileReference) {
			SpriteMapTileReference tileRef = (SpriteMapTileReference) reference;
			return tileRef.resolveSprite();
		}

		// Standard asset resolution for regular sprite references
		return getAsset(reference);
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
				throw new AssetTypeException(assetId, asset.getClass(), existing.type);
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
			throw new AssetNotFoundException(assetId, null);
		}

		// Load asset
		IAssetLoader<T> loader = (IAssetLoader<T>) assetLoaders.get(descriptor.getAssetType());
		if (loader == null) {
			throw new AssetLoaderNotFoundException(descriptor.getAssetType());
		}

		T asset = loader.loadAsset(descriptor);
		if (asset == null) {
			throw new AssetLoadException(assetId);
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

	/**
	 * Type mismatch exception class.
	 */
	public static class AssetTypeException extends IllegalArgumentException {
		public AssetTypeException(String assetId, Class<?> requestedType, Class<?> actualType) {
			super(String.format("Type mismatch: Asset '%s' is of type %s but requested as %s",
				assetId, actualType.getName(), requestedType.getName()));
		}
	}

	/**
	 * Asset not found exception class.
	 */
	public static class AssetNotFoundException extends IllegalArgumentException {
		public AssetNotFoundException(String assetId, Class<?> assetType) {
			super(String.format("No asset descriptor found for: %s%s",
				assetId, assetType != null ? " of type " + assetType.getName() : ""));
		}
	}

	/**
	 * Asset loader not found exception class.
	 */
	public static class AssetLoaderNotFoundException extends IllegalArgumentException {
		public AssetLoaderNotFoundException(Class<?> assetType) {
			super(String.format("No loader found for asset type: %s", assetType.getName()));
		}
	}

	/**
	 * Asset load exception class.
	 */
	public static class AssetLoadException extends IllegalStateException {
		public AssetLoadException(String assetId) {
			super(String.format("Asset loader returned null for: %s", assetId));
		}
	}
}