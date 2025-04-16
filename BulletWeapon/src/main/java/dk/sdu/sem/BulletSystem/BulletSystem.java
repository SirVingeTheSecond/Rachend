package dk.sdu.sem.BulletSystem;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Scene;
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
		// sort the set for later use
		Set<BulletNode> bulletNodes = NodeManager.active().getNodes(BulletNode.class);

		/*
		for (BulletNode bulletNode : bulletNodes) {
			Vector2D forward = bulletNode.transformComponent.forward();


			float speed = (float) (bulletNode.bulletComponent.getSpeed() * Time.getDeltaTime());
			bulletNode.transformComponent.translate(forward.scale(speed));



			// TODO: kill bullets after a lifetime or when the time for the
			// either after some time, or when the player leaves the room or
			// rather the scene.
//			bulletNode.getEntity().getComponent(BulletComponent.class)
//				Scene.getActiveScene().getEntitiesWithComponent(BulletComponent.class)

		}*/
	}

	/// Debug render bullets because they don't have sprites at the moment
	@Override
	public void onGUI(GraphicsContext gc) {
		/*
		Set<BulletNode> bulletNodes = NodeManager.active().getNodes(BulletNode.class);

		for (BulletNode bulletNode : bulletNodes) {
			Vector2D position = bulletNode.transformComponent.getPosition();
			gc.fillOval(position.x(), position.y(), 5, 5);
		}*/
	}

	}

