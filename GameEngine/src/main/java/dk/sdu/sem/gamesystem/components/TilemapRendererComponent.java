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
	private SpriteMap spriteMap;   // Cached sprite map
	private int renderLayer = 10;  // Default render layer
	private boolean snapshotValid = false; // Flag to indicate if the snapshot is valid

	/**
	 * Creates a tilemap renderer for a tilemap component.
	 */
	public TilemapRendererComponent(TilemapComponent tilemapData) {
		this.tilemapData = tilemapData;
		loadSpriteMap();
	}

	/**
	 * Loads the sprite map from the tilemap's tileset ID.
	 */
	private void loadSpriteMap() {
		if (tilemapData.getTilesetId() != null) {
			try {
				this.spriteMap = AssetFacade.createSpriteMap(tilemapData.getTilesetId())
					.withAutoDetectTileSize()
					.load();
			} catch (Exception e) {
				System.err.println("Failed to load sprite map for tileset: " + tilemapData.getTilesetId());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Gets the associated tilemap data component.
	 */
	public TilemapComponent getTilemapData() {
		return tilemapData;
	}

	/**
	 * Gets the sprite map used for rendering.
	 */
	public SpriteMap getSpriteMap() {
		if (spriteMap == null) {
			loadSpriteMap();
		}
		return spriteMap;
	}

	/**
	 * Gets the render layer.
	 */
	public int getRenderLayer() {
		return renderLayer;
	}

	/**
	 * Sets the render layer.
	 */
	public void setRenderLayer(int renderLayer) {
		this.renderLayer = renderLayer;
	}

	/**
	 * Gets a sprite for a tile, taking animations into account.
	 * If the tile is animated, returns the current frame of the animation.
	 * Otherwise, returns the static sprite from the sprite map.
	 *
	 * @param tileId The ID of the tile to get
	 * @return The sprite to render for the tile
	 */
	public Sprite getTileSprite(TileAnimatorComponent animComponent, int tileId) {
		// Check if this tile is animated
		if (animComponent != null && animComponent.hasTileAnimation(tileId)) {
			// Get the current frame sprite from the animation
			Sprite animatedSprite = animComponent.getCurrentFrameSprite(tileId);
			if (animatedSprite != null) {
				return animatedSprite;
			}
		}

		// Fall back to static sprite from sprite map
		if (spriteMap != null) {
			return spriteMap.getTile(tileId);
		}

		return null;
	}

	/**
	 * Checks if the snapshot of this tilemap is valid.
	 * Used by rendering system to determine if it needs to redraw the tilemap.
	 *
	 * @return True if the snapshot is valid, false if it needs to be redrawn
	 */
	public boolean isSnapshotValid() {
		return snapshotValid;
	}

	/**
	 * Marks the snapshot as valid after it has been drawn.
	 */
	public void markSnapshotValid() {
		snapshotValid = true;
	}

	/**
	 * Invalidates the snapshot, forcing the tilemap to be redrawn.
	 * This should be called when animated tiles update.
	 */
	public void invalidateSnapshot() {
		snapshotValid = false;
	}
}