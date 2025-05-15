package dk.sdu.sem.meleeweaponsystem;

import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;

public class MeleeAssetProvider implements IAssetProvider {
	@Override
	public void provideAssets() {
		// Create individual sprite frames
		AssetFacade.createSprite("sweep_anim_f0")
			.withImagePath("All_Fire_Bullet_Pixel_16x16_00")
			.withSourceRect(141, 131, 16, 16)
			.load();

		AssetFacade.createSprite("sweep_anim_f1")
			.withImagePath("All_Fire_Bullet_Pixel_16x16_00")
			.withSourceRect(152, 131, 16, 16)
			.load();

		AssetFacade.createSprite("sweep_anim_f2")
			.withImagePath("All_Fire_Bullet_Pixel_16x16_00")
			.withSourceRect(152, 138, 16, 16)
			.load();

		AssetFacade.createSprite("sweep_anim_f3")
			.withImagePath("All_Fire_Bullet_Pixel_16x16_00")
			.withSourceRect(147, 138, 16, 16)
			.load();

		// main slash
		AssetFacade.createAnimation("melee_swipe")
			.withFrames(
				"sweep_anim_f0",
				"sweep_anim_f1",
				"sweep_anim_f2",
				"sweep_anim_f3"
			)
			.withFrameDuration(0.5f)
			.withLoop(false)
			.load();

		// partial swipe
		AssetFacade.createAnimation("beg_partialSwipe")
			.withFrames(
				"sweep_anim_f0",
				"sweep_anim_f1"
			)
			.withFrameDuration(0.2f)
			.withLoop(false)
			.load();
	}
}