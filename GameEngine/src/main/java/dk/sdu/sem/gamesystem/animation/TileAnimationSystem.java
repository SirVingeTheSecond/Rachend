package dk.sdu.sem.gamesystem.animation;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.TileAnimatorComponent;
import dk.sdu.sem.gamesystem.data.TilemapNode;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.Set;

/**
 * System that manages tile animations in tilemaps.
 */
public class TileAnimationSystem implements IUpdate {

	@Override
	public void update() {
		// Get all tilemap nodes
		Set<TilemapNode> tilemapNodes = NodeManager.active().getNodes(TilemapNode.class);

		for (TilemapNode node : tilemapNodes) {
			// Skip if the node doesn't have a TileAnimationComponent
			TileAnimatorComponent animComponent = node.getEntity().getComponent(TileAnimatorComponent.class);
			if (animComponent == null) {
				continue;
			}

			// Update all animations in the component
			float deltaTime = (float) Time.getDeltaTime();

			for (Integer tileId : animComponent.getAnimatedTileIds()) {
				animComponent.updateAnimationTime(tileId, deltaTime);
			}

			// Mark renderer's snapshot for update when animations have changed
			node.renderer.invalidateSnapshot();
		}
	}
}