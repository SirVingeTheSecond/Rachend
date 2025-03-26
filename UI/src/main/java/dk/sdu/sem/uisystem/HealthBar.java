package dk.sdu.sem.uisystem;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import dk.sdu.sem.gamesystem.services.IStart;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HealthBar implements IGUIUpdate, IStart, IAssetProvider {
	ArrayList<SpriteAnimation> hearts = new ArrayList<>();

	int lastHP;
	int lastMax;

	@Override
	public void onGUI(GraphicsContext gc) {
		HealthBarNode node = NodeManager.active().getNodes(HealthBarNode.class).stream().findFirst().orElse(null);
		if (node == null)
			return;

		int max = node.health.getMaxHealth();

		if (max > lastMax) {
			for (int i = 0; i < max - lastMax; i++) {
				SpriteAnimation animation = AssetFacade.loadAnimation("heart_lose");
				animation.setCurrentFrameIndex(animation.getFrameCount() - 1);
				hearts.add(animation);
			}
		}

		if (max < lastMax) {
			for (int i = 0; i < lastMax - max; i++) {
				hearts.remove(hearts.size() - 1);
			}
		}

		lastMax = max;

		int hp = node.health.getHealth();

		if (hp < lastHP) {
			for (int i = hp; i < lastHP; i++) {
				if (hearts.size() > i)
					hearts.get(i).play();
			}
		}

		if (hp > lastHP) {
			for (int i = lastHP; i < hp; i++) {
				hearts.get(i).reset();
				hearts.get(i).pause();
			}
		}

		lastHP = hp;

		int i = 0;
		int j = 0;
		for (SpriteAnimation animation : hearts) {
			animation.update(Time.getDeltaTime());

			if (i > 5)
			{
				j++;
				i = 0;
			}

			gc.drawImage(animation.getCurrentFrame().getImage(), 0, 0, 17, 17, i * 34 + ((j % 2) * 8), j * 17, 34, 34);

			i++;
		}
	}

	@Override
	public void start() {

	}

	@Override
	public void provideAssets() {
		// First load each sprite individually to ensure it exists in the system
		List<String> heartFrames = Arrays.asList(
			"heart_1",
			"heart_2",
			"heart_3",
			"heart_4",
			"heart_5"
		);

		// Pre-load each frame as a sprite
		List<String> heartSpriteIds = new ArrayList<>();
		for (String frame : heartFrames) {
			// Load sprite explicitly first
			Sprite sprite = AssetFacade.loadSprite(frame);
			heartSpriteIds.add(sprite.getName());
		}

		// Now create the animation using the loaded sprites
		AssetFacade.createAnimation(
			"heart_lose",
			heartSpriteIds,
			0.1,
			false
		);
	}
}
