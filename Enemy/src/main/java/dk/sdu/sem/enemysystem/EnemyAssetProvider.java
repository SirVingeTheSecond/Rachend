package dk.sdu.sem.enemysystem;

import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;

import java.util.Arrays;

public class EnemyAssetProvider implements IAssetProvider {

	@Override
	public void provideAssets() {
		// Register the idle animation using the exact frame filenames
		AssetFacade.createAnimation(
			"demon_idle", // The animation name used for reference
			Arrays.asList(
				"big_demon_idle_anim_f0",
				"big_demon_idle_anim_f1",
				"big_demon_idle_anim_f2",
				"big_demon_idle_anim_f3"
			),
			0.1, // 0.1 seconds per frame = 10 FPS
			true // Loop the animation
		);

		// Register the idle animation using the exact frame filenames
		AssetFacade.createAnimation(
			"demon_run", // The animation name used for reference
			Arrays.asList(
				"big_demon_run_anim_f0",
				"big_demon_run_anim_f1",
				"big_demon_run_anim_f2",
				"big_demon_run_anim_f3"
			),
			0.1, // 0.1 seconds per frame = 10 FPS
			true // Loop the animation
		);
	}
}
