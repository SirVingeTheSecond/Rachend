package dk.sdu.sem.weaponsystem;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import dk.sdu.sem.gamesystem.services.IUpdate;
import javafx.scene.canvas.GraphicsContext;

import java.util.Set;

public class BulletSystem implements IUpdate, IGUIUpdate {

	@Override
	public void update() {
		Set<BulletNode> bulletNodes = NodeManager.active().getNodes(BulletNode.class);

		for (BulletNode bulletNode : bulletNodes) {
			Vector2D forward = bulletNode.transform.forward();


			float speed = (float) (bulletNode.bullet.getSpeed() * Time.getDeltaTime());
			bulletNode.transform.translate(forward.scale(speed));

			// TODO: kill bullets after a lifetime
		}
	}

	@Override
	public void onGUI(GraphicsContext gc) {
		Set<BulletNode> bulletNodes = NodeManager.active().getNodes(BulletNode.class);

		for (BulletNode bulletNode : bulletNodes) {
			Vector2D position = bulletNode.transform.getPosition();
			gc.fillOval(position.getX(), position.getY(), 5, 5);
		}
	}
}