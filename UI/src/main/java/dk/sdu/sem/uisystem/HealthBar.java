package dk.sdu.sem.uisystem;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import dk.sdu.sem.gamesystem.services.IStart;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;

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
				SpriteAnimation animation = AssetFacade.getAnimation("heart_animated_1");
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

			if (i > 5) {
				j++;
				i = 0;
			}

			gc.drawImage(animation.getCurrentFrame().getImage(), 0, 0, 17, 17, i * 34 + ((j % 2) * 8), j * 17, 34, 34);

			i++;
		}
	}

	@Override
	public void start() {
		// Empty implementation
	}

	@Override
	public void provideAssets() {
		SpriteMap map = AssetFacade.createSpriteMap("heart_animated_1")
			.withGrid(5, 1, 17, 17)
			.load();

		AssetFacade.createAnimation("heart_animated_1")
			.withSpriteMap(map)
			.withFrameDuration(0.1)
			.withLoop(false)
			.load();
	}
}