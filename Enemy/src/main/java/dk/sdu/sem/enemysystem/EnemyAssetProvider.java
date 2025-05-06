package dk.sdu.sem.enemysystem;

import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;

import java.util.Arrays;

public class EnemyAssetProvider implements IAssetProvider {

	@Override
	public void provideAssets() {

		// Register the idle animation using the exact frame filenames
		AssetFacade.createAnimation("demon_idle")
				.withFrames(
						"big_demon_idle_anim_f0",
						"big_demon_idle_anim_f1",
						"big_demon_idle_anim_f2",
						"big_demon_idle_anim_f3"
				)
				.withFrameDuration(0.1)
				.withLoop(true)
				.load();

		// Register the run animation using the exact frame filenames
		AssetFacade.createAnimation("demon_run")
				.withFrames(
						"big_demon_run_anim_f0",
						"big_demon_run_anim_f1",
						"big_demon_run_anim_f2",
						"big_demon_run_anim_f3"
				)
				.withFrameDuration(0.1)
				.withLoop(true)
				.load();


		AssetFacade.createAnimation("demon_hurt")
			.withFrames(
				"big_demon_hurt"
			)
			.withFrameDuration(0.05)
			.withLoop(false)
			.load();
	}
}
