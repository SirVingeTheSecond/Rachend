package dk.sdu.sem.meleeweaponsystem;

import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;

public class MeleeAssetProvider implements IAssetProvider {
	@Override
	public void provideAssets() {
		// This might make a duplicate of the spritemap if another IAssetProvider also creates a spritemap;
		SpriteMap map = AssetFacade.createSpriteMap("All_Fire_Bullet_Pixel_16x16_00")
			.withGrid(9,9,16,16)
			.load();

		AssetFacade.AnimationBuilder animationBuilder = AssetFacade.createAnimation("melee_swipe").withSpriteMap(map).withTileIndices();

		AssetFacade.createSprite("sweep_anim_f0")
			.withImagePath("All_Fire_Bullet_Pixel_16x16_00")
			.withSourceRect(141,131,16,16)
			.load();

		AssetFacade.createSprite("sweep_anim_f1")
			.withImagePath("All_Fire_Bullet_Pixel_16x16_00")
			.withSourceRect(152,131,16,16)
			.load();

		AssetFacade.createSprite("sweep_anim_f2")
			.withImagePath("All_Fire_Bullet_Pixel_16x16_00")
			.withSourceRect(152,138,16,16)
			.load();
		AssetFacade.createSprite("sweep_anim_f3")
			.withImagePath("All_Fire_Bullet_Pixel_16x16_00")
			.withSourceRect(147,138,16,16)
			.load();

		AssetFacade.createAnimation("melee_swipe")
			// implementationwise frames are the sprites datatype.
			.withFrames(
				"sweep_anim_f0",
				"sweep_anim_f1",
				"sweep_anim_f2",
				"sweep_anim_f3"
			)
			// this could be set based on weapon holders attack speed, as
			// higher duration
			// means attacking slower.
			.withFrameDuration(0.5)
			.withLoop(false)
			.load();
		// the animation to be used for when the swipe waepon is activated
		AssetFacade.createAnimation("beg_partialSwipe")
			.withFrames("sweep_anim_f0",
				"sweep_anim_f1")
			.withLoop(false)
			.withFrameDuration(0.2)
			.load();

		// just get an empty area
//		AssetFacade.createSprite("melee_null")
//			.withImagePath("All_Fire_Bullet_Pixel_16x16_00")
//				.withSourceRect(81,0,16,16).load();
//
//		AssetFacade.createAnimation("melee_null")
//			.withFrames("melee_null")
//			.withLoop(false)
//			.load();

	}
}
