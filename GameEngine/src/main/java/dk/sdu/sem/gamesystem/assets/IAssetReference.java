package dk.sdu.sem.gamesystem.assets;

/**
 * Interface for asset references.
 * Provides a type-safe way to reference assets without loading them.
 */
public interface IAssetReference<T> {
	/**
	 * Gets the ID for this asset.
	 */
	String getAssetId();

	/**
	 * Gets the type of the asset.
	 */
	Class<T> getAssetType();
}