package dk.sdu.sem.BulletSystem;

import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;

public class BulletAssetProvider implements IAssetProvider {
	@Override
	public void provideAssets() {
		SpriteMap map = AssetFacade.createSpriteMap("fire_bullet_sheet")
			.withGrid(4,1,16,16)
			.load();

		AssetFacade.createAnimation("fire_bullet_anim")
			.withSpriteMap(map)
			.withFrameDuration(0.1f)
			.withLoop(true)
			.load();
	}
}
