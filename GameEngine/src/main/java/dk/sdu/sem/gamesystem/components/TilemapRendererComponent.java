package dk.sdu.sem.gamesystem.components;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commontilemap.TilemapComponent;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;

/**
 * Component that handles rendering of a tilemap with animation support.
 */
public class TilemapRendererComponent implements IComponent {

	private final TilemapComponent tilemapData;
	private SpriteMap spriteMap;                 // cached sprites
	private int renderLayer = 10;                // default render layer
	private boolean snapshotValid = false;

	public TilemapRendererComponent(TilemapComponent tilemapData) {
		this.tilemapData = tilemapData;
		loadSpriteMap();
	}

	private void loadSpriteMap() {
		if (tilemapData.getTilesetId() != null) {
			spriteMap = AssetFacade.createSpriteMap(tilemapData.getTilesetId())
				.withAutoDetectTileSize()
				.load(); // already cached by AssetFacade
		}
	}

	public TilemapComponent getTilemapData() { return tilemapData; }

	public SpriteMap getSpriteMap() {
		if (spriteMap == null) loadSpriteMap();
		return spriteMap;
	}

	public int getRenderLayer() {
		return renderLayer;
	}

	public void setRenderLayer(int layer) {
		this.renderLayer = layer;
	}

	/**
	 * Returns the correct sprite for the tile currently on the map.
	 * Empty tiles (tileId < 0) are ignored.
	 */
	public Sprite getTileSprite(TileAnimatorComponent anim, int tileId) {

		if (tileId < 0) {
			return null;
		}

		if (anim != null && anim.hasTileAnimation(tileId)) {
			Sprite animated = anim.getCurrentFrameSprite(tileId);
			if (animated != null) return animated;
		}
		return (spriteMap != null) ? spriteMap.getTile(tileId) : null;
	}

	public boolean isSnapshotValid() {
		return snapshotValid;
	}

	public void markSnapshotValid() {
		snapshotValid = true; }

	public void invalidateSnapshot() {
		snapshotValid = false;
	}
}