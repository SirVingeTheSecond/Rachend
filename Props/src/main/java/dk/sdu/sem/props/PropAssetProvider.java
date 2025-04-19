package dk.sdu.sem.props;

import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;

public class PropAssetProvider implements IAssetProvider {
	@Override
	public void provideAssets() {
		AssetFacade.createSprite("vase1")
			.withImagePath("vase1.png")
			.load();

		AssetFacade.createSprite("vase1_broken")
			.withImagePath("vase1_broken.png")
			.load();

		AssetFacade.createSprite("vase2")
			.withImagePath("vase2.png")
			.load();

		AssetFacade.createSprite("vase2_broken")
			.withImagePath("vase2_broken.png")
			.load();

		AssetFacade.createSprite("box1")
			.withImagePath("box1.png")
			.load();

		AssetFacade.createSprite("box1_broken")
			.withImagePath("box1_broken.png")
			.load();
	}
}
