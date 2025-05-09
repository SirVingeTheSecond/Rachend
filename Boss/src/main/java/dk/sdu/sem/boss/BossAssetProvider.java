package dk.sdu.sem.boss;

import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;

public class BossAssetProvider implements IAssetProvider {
	@Override
	public void provideAssets() {
		AssetFacade.createAnimation("boss_idle")
			.withFrames(
				"necromancer_anim_f0",
				"necromancer_anim_f1",
				"necromancer_anim_f2",
				"necromancer_anim_f3"
			)
			.withFrameDuration(0.1)
			.withLoop(true)
			.load();

		AssetFacade.createAnimation("boss_hurt")
			.withFrames(
				"necromancer_hurt"
			)
			.withFrameDuration(0.05)
			.withLoop(false)
			.load();
	}
}
