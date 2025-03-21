package dk.sdu.sem.playersystem;

import dk.sdu.sem.gamesystem.assets.registry.AssetRegistrar;
import dk.sdu.sem.gamesystem.assets.registry.IAssetRegistryProvider;

import java.util.Arrays;

/**
 * Asset registry provider for player-specific assets.
 * This is implemented in the Player module to register its own assets.
 */
public class PlayerAssetsProvider implements IAssetRegistryProvider {
	@Override
	public void registerAssets(AssetRegistrar registrar) {
		// Register player image assets
		registrar.registerImage("elf_m_run_anim_f0", "elf_m_run_anim_f0.png");
		registrar.registerImage("elf_m_run_anim_f1", "elf_m_run_anim_f1.png");
		registrar.registerImage("elf_m_run_anim_f2", "elf_m_run_anim_f2.png");
		registrar.registerImage("elf_m_run_anim_f3", "elf_m_run_anim_f3.png");

		registrar.registerImage("elf_m_idle_anim_f0", "elf_m_idle_anim_f0.png");
		registrar.registerImage("elf_m_idle_anim_f1", "elf_m_idle_anim_f1.png");
		registrar.registerImage("elf_m_idle_anim_f2", "elf_m_idle_anim_f2.png");
		registrar.registerImage("elf_m_idle_anim_f3", "elf_m_idle_anim_f3.png");

		// Register player sprites
		registrar.registerSprite("elf_run_0", "elf_m_run_anim_f0");
		registrar.registerSprite("elf_run_1", "elf_m_run_anim_f1");
		registrar.registerSprite("elf_run_2", "elf_m_run_anim_f2");
		registrar.registerSprite("elf_run_3", "elf_m_run_anim_f3");

		registrar.registerSprite("elf_idle_0", "elf_m_idle_anim_f0");
		registrar.registerSprite("elf_idle_1", "elf_m_idle_anim_f1");
		registrar.registerSprite("elf_idle_2", "elf_m_idle_anim_f2");
		registrar.registerSprite("elf_idle_3", "elf_m_idle_anim_f3");

		// Register player animations
		registrar.registerAnimation(
			"elf_run_animation",
			Arrays.asList("elf_run_0", "elf_run_1", "elf_run_2", "elf_run_3"),
			0.15, // frameDuration
			true  // looping
		);

		registrar.registerAnimation(
			"elf_idle_animation",
			Arrays.asList("elf_idle_0", "elf_idle_1", "elf_idle_2", "elf_idle_3"),
			0.25, // frameDuration
			true  // looping
		);

		// Alternative: Register a spritesheet for animations
		registrar.registerImage("elf_spritesheet", "elf_spritesheet.png");
		registrar.registerSpriteMap("elf_sprites", "elf_spritesheet", 4, 4, 16, 16);

		// Example of getting sprites from the map
		for (int i = 0; i < 4; i++) {
			registrar.registerSpriteFromMap(
				"elf_run_sheet_" + i,
				"elf_sprites",
				"tile_" + i + "_0"  // First row is run animation
			);

			registrar.registerSpriteFromMap(
				"elf_idle_sheet_" + i,
				"elf_sprites",
				"tile_" + i + "_1"  // Second row is idle animation
			);
		}

		// Example of animations from spritesheet
		registrar.registerAnimation(
			"elf_run_sheet_animation",
			Arrays.asList("elf_run_sheet_0", "elf_run_sheet_1", "elf_run_sheet_2", "elf_run_sheet_3"),
			0.15,
			true
		);
	}
}