package dk.sdu.sem.BulletSystem;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import dk.sdu.sem.gamesystem.services.IUpdate;
import javafx.scene.canvas.GraphicsContext;

import java.util.Set;

public class BulletSystem implements IUpdate, IGUIUpdate {

	/// Move bullets
	@Override
	public void update() {
		Set<BulletNode> bulletNodes = NodeManager.active().getNodes(BulletNode.class);

		for (BulletNode bulletNode : bulletNodes) {
			Vector2D forward = bulletNode.transformComponent.forward();


			float speed = (float) (bulletNode.bulletComponent.getSpeed() * Time.getDeltaTime());
			bulletNode.transformComponent.translate(forward.scale(speed));

			// TODO: kill bullets after a lifetime
		}
	}

	/// Debug render bullets because they don't have sprites at the moment
	@Override
	public void onGUI(GraphicsContext gc) {
		Set<BulletNode> bulletNodes = NodeManager.active().getNodes(BulletNode.class);

		for (BulletNode bulletNode : bulletNodes) {
			Vector2D position = bulletNode.transformComponent.getPosition();
			gc.fillOval(position.x(), position.y(), 5, 5);
		}
	}

	}

