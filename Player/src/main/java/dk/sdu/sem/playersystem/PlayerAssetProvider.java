package dk.sdu.sem.playersystem;

import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

/**
 * Asset provider for player assets.
 */
public class PlayerAssetProvider implements IAssetProvider {
	private static final Logging LOGGER = Logging.createLogger("PlayerAssetProvider", LoggingLevel.DEBUG);

	@Override
	public void provideAssets() {
	/*
		SpriteMap playerSpriteMap = AssetFacade.createSpriteMap("Test")
			.withGrid(5, 1, 17, 17)
			.load();

		AssetFacade.createAnimation("player_idle")
			.withSpriteMap(playerSpriteMap)
			.withFrameDuration(0.1)
			.withLoop(true)
			.load();
	 */
		AssetFacade.createAnimation("player_idle")
			.withFrames(
				"elf_m_idle_anim_f0",
				"elf_m_idle_anim_f1",
				"elf_m_idle_anim_f2",
				"elf_m_idle_anim_f3"
			)
			.withFrameDuration(0.1)
			.withLoop(true)
			.load();

		createRunAnimation();
	}

	private void createRunAnimation() {
		AssetFacade.createAnimation("player_run")
			.withFrames(
				"elf_m_run_anim_f0",
				"elf_m_run_anim_f1",
				"elf_m_run_anim_f2",
				"elf_m_run_anim_f3"
			)
			.withFrameDuration(0.08)
			.withLoop(true)
			.load();

		LOGGER.debug("Created run animation from individual frames");
	}
}