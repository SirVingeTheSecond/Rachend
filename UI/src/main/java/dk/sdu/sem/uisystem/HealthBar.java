package dk.sdu.sem.uisystem;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.input.Key;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import dk.sdu.sem.gamesystem.services.IStart;
import javafx.geometry.Rectangle2D;
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

		int max = (int)node.stats.getMaxHealth();

		if (max > lastMax && max > 0) {
			for (int i = 0; i < max - lastMax; i++) {
				SpriteAnimation animation = AssetFacade.getAnimation("heart_animated_1");
				animation.setCurrentFrameIndex(animation.getFrameCount() - 1);
				hearts.add(animation);
			}
		}

		if (max < lastMax && max >= 0) {
			for (int i = 0; i < lastMax - max; i++) {
				hearts.remove(hearts.size() - 1);
			}
		}

		lastMax = max;

		int hp = (int)node.stats.getCurrentHealth();

		if (hp < lastHP && hp >= 0) {
			for (int i = hp; i < lastHP; i++) {
				if (hearts.size() > i)
					hearts.get(i).play();
			}
		}

		if (hp > lastHP && hp > 0) {
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

			Rectangle2D rect = animation.getCurrentFrame().getSourceRect();
			gc.drawImage(animation.getCurrentFrame().getImage(), rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight(), i * 34 + ((j % 2) * 8), j * 17, 34, 34);

			i++;
		}

		// Debug controls
		/*
		if (Input.getKeyDown(Key.MOUSE1)) {
			node.stats.setCurrentHealth(node.stats.getCurrentHealth() -1);
		}
		if (Input.getKeyDown(Key.MOUSE2)) {
			node.stats.setCurrentHealth(node.stats.getCurrentHealth() + 1);
		}
		if (Input.getKeyDown(Key.UP)) {
			node.stats.setMaxHealth(node.stats.getMaxHealth() + 1);
		}
		if (Input.getKeyDown(Key.DOWN)) {
			node.stats.setMaxHealth(node.stats.getMaxHealth() - 1);
		}
		 */
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