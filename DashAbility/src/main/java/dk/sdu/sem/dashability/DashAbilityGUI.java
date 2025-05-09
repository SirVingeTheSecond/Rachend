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
			// Only render the cooldown bar when the dash is on cooldown
			if (node.dash.isOnCooldown()) {
				Vector2D position = node.transform.getPosition();

				gc.save();

				gc.setFill(Color.WHITE);
				gc.translate(position.x() - 8, position.y() + 20);
				gc.fillRect(0, 0, node.dash.progress() * 16, 6);

				gc.restore();
			}
		});
	}
}