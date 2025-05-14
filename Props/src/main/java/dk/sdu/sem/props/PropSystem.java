package dk.sdu.sem.props;

import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.Set;

public class PropSystem implements IUpdate {

	@Override
	public void update() {
		Set<BreakableNode> nodes = NodeManager.active().getNodes(BreakableNode.class);
		for (BreakableNode node : nodes) {

			if (node.stats.getCurrentHealth() <= 0)
				breakProp(node);
		}
	}

	private void breakProp(BreakableNode node) {
		if (node.prop.getBrokenSprite() == null) {
			node.getEntity().getScene().removeEntity(node.getEntity());
			return;
		}

		node.renderer.setSprite(node.prop.getBrokenSprite());
		//Always place behind other objects when broken
		node.renderer.setRenderLayer(GameConstants.LAYER_OBJECTS - 1);

		//Remove collider when broken
		ColliderComponent collider = node.getEntity().getComponent(ColliderComponent.class);
		if (collider != null)
			collider.setLayer(PhysicsLayer.DECORATION);

		//No need to keep the break component once broken
		node.getEntity().removeComponent(PropBreakComponent.class);
	}
}
