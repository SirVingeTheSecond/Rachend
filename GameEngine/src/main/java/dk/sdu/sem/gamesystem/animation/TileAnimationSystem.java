package dk.sdu.sem.gamesystem.animation;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.TileAnimatorComponent;
import dk.sdu.sem.gamesystem.data.TilemapNode;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.Set;

/**
 * System that manages tile animations in tilemaps.
 * This updates animated tiles each frame to create the animation effect.
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

				// Get the current frame for the animation
				Sprite currentFrame = animComponent.getCurrentFrameSprite(tileId);

				// If we have a valid frame, update the tile in the renderer's cache
				// This is done indirectly - the renderer will pick up the animation's current frame
				// during the next render cycle
			}

			// Mark renderer's snapshot for update when animations have changed
			node.renderer.invalidateSnapshot();
		}
	}
}