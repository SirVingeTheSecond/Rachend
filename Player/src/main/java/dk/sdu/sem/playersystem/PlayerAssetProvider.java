package dk.sdu.sem.playersystem;

import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
import dk.sdu.sem.gamesystem.rendering.Sprite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Asset provider for player-related assets.
 */
public class PlayerAssetProvider implements IAssetProvider {
	@Override
	public void provideAssets() {
		// First load each sprite individually to ensure it exists in the system
		List<String> idleFrames = Arrays.asList(
			"elf_m_idle_anim_f0",
			"elf_m_idle_anim_f1",
			"elf_m_idle_anim_f2",
			"elf_m_idle_anim_f3"
		);

		// Pre-load each frame as a sprite
		List<String> idleSpriteIds = new ArrayList<>();
		for (String frame : idleFrames) {
			// Load sprite explicitly
			Sprite sprite = AssetFacade.loadSprite(frame);
			// Use the sprite's name - it's already properly namespaced internally
			idleSpriteIds.add(sprite.getName());
		}

		// Now create the animation using the loaded sprites
		AssetFacade.createAnimation(
			"player_idle", // The animation name
			idleSpriteIds, // Use the loaded sprite IDs
			0.1, // 0.1 seconds per frame = 10 FPS
			true // Loop the animation
		);

		// Same approach for run animation
		List<String> runFrames = Arrays.asList(
			"elf_m_run_anim_f0",
			"elf_m_run_anim_f1",
			"elf_m_run_anim_f2",
			"elf_m_run_anim_f3"
		);

		// Pre-load each frame as a sprite
		List<String> runSpriteIds = new ArrayList<>();
		for (String frame : runFrames) {
			Sprite sprite = AssetFacade.loadSprite(frame);
			runSpriteIds.add(sprite.getName());
		}

		// Create animation with loaded sprites
		AssetFacade.createAnimation(
			"player_run", // The animation name
			runSpriteIds, // Use the loaded sprite IDs
			0.08, // 0.08 seconds per frame = 12.5 FPS
			true // Loop the animation
		);
	}
}