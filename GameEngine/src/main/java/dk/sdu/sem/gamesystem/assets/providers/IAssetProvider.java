package dk.sdu.sem.gamesystem.assets.providers;

import dk.sdu.sem.gamesystem.assets.AssetFacade;

public interface IAssetProvider {
	/**
	 * Called during initialization to provide assets to the system.
	 * Use the {@link AssetFacade} class to define assets in this method.
	 */
	void provideAssets();
}