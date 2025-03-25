package dk.sdu.sem.playersystem;

import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;

import java.util.Arrays;

/**
 * Asset provider for player-related assets.
 */
public class PlayerAssetProvider implements IAssetProvider {
	@Override
	public void provideAssets() {
		// Register the idle animation using the exact frame filenames
		AssetFacade.createAnimation(
				"player_idle", // The animation name we'll reference later
				Arrays.asList(
						"elf_m_idle_anim_f0",
						"elf_m_idle_anim_f1",
						"elf_m_idle_anim_f2",
						"elf_m_idle_anim_f3"
				),
				0.1, // 0.1 seconds per frame = 10 FPS
				true // Loop the animation
		);

		// Register the run animation using the exact frame filenames
		AssetFacade.createAnimation(
				"player_run", // The animation name we'll reference later
				Arrays.asList(
						"elf_m_run_anim_f0",
						"elf_m_run_anim_f1",
						"elf_m_run_anim_f2",
						"elf_m_run_anim_f3"
				),
				0.08, // 0.08 seconds per frame = 12.5 FPS (slightly faster than idle)
				true // Loop the animation
		);
	}
}