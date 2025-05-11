package dk.sdu.sem.boss;

import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class BossBar implements IGUIUpdate {
	private Image bossBarDecoration = new Image(BossBar.class.getResourceAsStream("/boss_bar_decoration.png"));
	private Image bossBar = new Image(BossBar.class.getResourceAsStream("/boss_bar.png"));

	private float scale = 2;

	float xPos = (float) (((GameConstants.WORLD_SIZE.x() * GameConstants.TILE_SIZE) / 2f) - bossBarDecoration.getWidth() * scale / 2f);

	@Override
	public void onGUI(GraphicsContext gc) {
		float y = 0;
		for (BossNode node : NodeManager.active().getNodes(BossNode.class)) {
			gc.drawImage(bossBarDecoration, xPos, 0, bossBarDecoration.getWidth() * scale, bossBarDecoration.getHeight() * scale);

			float hp = node.stats.getStat(StatType.CURRENT_HEALTH);
			float maxHealth = node.stats.getStat(StatType.MAX_HEALTH);

			float ratio = hp / maxHealth;

			gc.drawImage(
				bossBar,
				0,
				0,
				bossBar.getWidth() * ratio,
				bossBar.getHeight(),
				xPos + 14 * scale,
				y,
				bossBar.getWidth() * scale * ratio,
				bossBar.getHeight() * scale
			);

			y += (float) (bossBarDecoration.getHeight() * scale + 20);
		}


	}
}
