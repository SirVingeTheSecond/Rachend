package dk.sdu.sem.inventory;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Map;
import java.util.Set;

/**
 * Displays the player's inventory.
 */
// Very basic example
public class InventoryDisplay implements IGUIUpdate {

	@Override
	public void onGUI(GraphicsContext gc) {
		Set<PlayerInventoryNode> playerNodes = NodeManager.active().getNodes(PlayerInventoryNode.class);

		if (playerNodes.isEmpty()) {
			return;
		}

		PlayerInventoryNode playerNode = playerNodes.iterator().next();

		gc.setFill(new Color(0, 0, 0, 0.5));
		gc.fillRect(10, 10, 200, 100);

		gc.setFill(Color.WHITE);
		gc.setFont(new Font("Arial", 16));
		gc.fillText("Inventory", 20, 30);

		// Draw items
		 gc.setFont(new Font("Arial", 12));
		int y = 50;
		for (int i = 0; i <= playerNode.inventory.getItemCount(); i++) {
			gc.fillText(playerNode.inventory.getItemInInventory(i).toString(), 20, y);
			y += 20;
		}
		/*for (Map.Entry<String, Integer> entry : playerNode.inventory.getItems().entrySet()) {
			gc.fillText(entry.getKey() + ": " + entry.getValue(), 20, y);
			y += 20;
		} */

		gc.fillText("Capacity: " + playerNode.inventory.getItemCount() + "/" +
			playerNode.inventory.getInventorySize(), 20, y + 10);
	}
}