package dk.sdu.sem.MeleeEnemy;

import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;

public class MeleeEnemyAssetProvider implements IAssetProvider {

	@Override
	public void provideAssets() {

		AssetFacade.createAnimation("meleeenemy_idle")
			.withFrames("ogre_idle_anim_f0","ogre_idle_anim_f1",
				"ogre_idle_anim_f2","ogre_idle_anim_f3")
			.withLoop(true)
			.load();

		AssetFacade.createAnimation("meleeenemy_run")
			.withFrames("ogre_run_anim_f0.png",
				"ogre_run_anim_f1.png",
				"ogre_run_anim_f2.png",
			"ogre_run_anim_f3.png")
			.withLoop(true)
			.load();

	}
}
