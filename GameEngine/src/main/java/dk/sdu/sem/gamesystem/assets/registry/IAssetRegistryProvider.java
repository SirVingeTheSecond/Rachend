package dk.sdu.sem.gamesystem.assets.registry;

/**
 * Interface for asset registry providers.
 * Each module can implement this to register its assets.
 */
public interface IAssetRegistryProvider {
	/**
	 * Called during initialization to register assets.
	 * @param registrar The registry to register assets with
	 */
	void registerAssets(AssetRegistrar registrar);
}