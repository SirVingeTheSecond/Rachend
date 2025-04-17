package dk.sdu.sem.gamesystem.components;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commontilemap.TilemapComponent;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;

/**
 * Component that handles rendering of a tilemap.
 */
public class TilemapRendererComponent implements IComponent {
	private final TilemapComponent tilemapData;
	private SpriteMap spriteMap;   // Cached sprite map
	private int renderLayer = 10;  // Default render layer

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
}