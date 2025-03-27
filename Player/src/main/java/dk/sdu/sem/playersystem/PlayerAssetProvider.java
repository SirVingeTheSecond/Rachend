package dk.sdu.sem.playersystem;

import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Asset provider for player-related assets.
 * Demonstrates direct sprite loading when needed.
 */
public class PlayerAssetProvider implements IAssetProvider {
	@Override
	public void provideAssets() {
		SpriteMap playerSpriteMap = AssetFacade.createSpriteSheet(
			"Test",     // Image name
			17, 17      // Tile width and height
		);

		AssetFacade.createAnimationFromSpriteMap(
			"player_idle",    // Animation name
			playerSpriteMap,  // The sprite map
			0.1,              // 0.1 seconds per frame = 10 FPS
			true              // Loop the animation
		);

		createRunAnimation();
	}

	/**
	 * Creates the run animation using either a sprite map or individual frames.
	 */
	private void createRunAnimation() {
		try {
			SpriteMap runSpriteMap = AssetFacade.createSpriteSheet(
				"Run",         // Image name
				17, 17         // Tile width and height
			);

			AssetFacade.createAnimationFromSpriteMap(
				"player_run",    // Animation name
				runSpriteMap,    // The sprite map
				0.08,            // 0.08 seconds per frame = 12.5 FPS
				true             // Loop the animation
			);

			System.out.println("Created run animation from sprite map");
		} catch (Exception e) {
			System.out.println("No sprite map found for run animation, using individual frames");

			AssetFacade.createAnimationWithPreloading(
				"player_run",           // Animation name
				new String[] {          // Frame names
					"elf_m_run_anim_f0",
					"elf_m_run_anim_f1",
					"elf_m_run_anim_f2",
					"elf_m_run_anim_f3"
				},
				0.08,                   // 0.08 seconds per frame
				true                    // Loop the animation
			);

			System.out.println("Created run animation from individual frames");
		}
	}
}