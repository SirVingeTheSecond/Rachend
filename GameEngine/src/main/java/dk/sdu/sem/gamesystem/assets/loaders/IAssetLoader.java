package dk.sdu.sem.gamesystem.assets.loaders;

import dk.sdu.sem.gamesystem.assets.AssetDescriptor;

/**
 * Interface for asset loaders.
 * Each asset type should have a corresponding loader implementation.
 */
public interface IAssetLoader<T> {
	/**
	 * Gets the type of asset this loader handles.
	 */
	Class<T> getAssetType();

	/**
	 * Loads an asset from its descriptor.
	 */
	T loadAsset(AssetDescriptor<T> descriptor);
}