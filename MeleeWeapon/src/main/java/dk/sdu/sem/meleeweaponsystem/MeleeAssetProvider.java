package dk.sdu.sem.meleeweaponsystem;

import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;

public class MeleeAssetProvider implements IAssetProvider {
	@Override
	public void provideAssets() {
		// This might make a duplicate of the spritemap if another IAssetProvider also creates a spritemap;
		SpriteMap map = AssetFacade.createSpriteMap("fire_bullet_sheet")
			.withGrid(9,9,16,16)
			.load();

		AssetFacade.AnimationBuilder animationBuilder = AssetFacade.createAnimation("melee_swipe").withSpriteMap(map).withTileIndices();


		// where do I set the source rect ?
		// 12/13
//			.withSourceRect(147,131,159,144);
		// the start of the animation
		Sprite swipeSprite0 = AssetFacade.createSprite("sweep_anim_f0")
			.withImagePath("fire_bullet_sheet")
			.withSourceRect(141,131,152,134)
			.load();

		Sprite swipeSprite1 = AssetFacade.createSprite("sweep_anim_f1")
			.withImagePath("fire_bullet_sheet")
			.withSourceRect(152,131,158,137)
			.load();

		Sprite swipeSprite2 = AssetFacade.createSprite("sweep_anim_f2")
			.withImagePath("fire_bullet_sheet")
			.withSourceRect(152,138,159,144)
			.load();
		Sprite swipeSprite3 = AssetFacade.createSprite("sweep_anim_f3")
			.withImagePath("fire_bullet_sheet")
			.withSourceRect(147,138,152,144)
			.load();
//		AssetFacade.createSpriteReference("sweep_anim_f0");

		AssetFacade.createAnimation("melee_swipe")
			.withFrames(
				"sweep_anim_f0",
				"sweep_anim_f1",
				"sweep_anim_f2",
				"sweep_anim_f3"
			)
			.withFrameDuration(0.2)
			.withLoop(false)
			.load();
	}
}
