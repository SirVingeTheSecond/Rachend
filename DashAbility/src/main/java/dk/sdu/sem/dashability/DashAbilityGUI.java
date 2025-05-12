package dk.sdu.sem.dashability;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Set;

public class DashAbilityGUI implements IGUIUpdate {
	@Override
	public void onGUI(GraphicsContext gc) {
		Set<DashAbilityNode> nodes = NodeManager.active().getNodes(DashAbilityNode.class);

		nodes.forEach(node -> {
			// Render the cooldown bar when it's visible (either on cooldown or fading)
			if (node.dash.isBarVisible()) {
				Vector2D position = node.transform.getPosition();

				gc.save();

				Color barColor = Color.WHITE;
				if (node.dash.getFadeOpacity() < 1.0) {
					barColor = new Color(
						barColor.getRed(),
						barColor.getGreen(),
						barColor.getBlue(),
						node.dash.getFadeOpacity()
					);
				}

				gc.setFill(barColor);
				gc.translate(position.x() - 8, position.y() + 20);

				// When fading, show full bar width
				double width = node.dash.isOnCooldown() ? node.dash.progress() * 16 : 16;

				gc.fillRect(0, 0, width, 6);

				gc.restore();
			}
		});
	}
}