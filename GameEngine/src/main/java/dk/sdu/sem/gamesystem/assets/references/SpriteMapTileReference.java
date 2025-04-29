package dk.sdu.sem.gamesystem.assets.references;

import dk.sdu.sem.gamesystem.assets.managers.AssetManager;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

/**
 * Reference to a specific tile within a sprite map.
 * This allows animations to reference sprites that exist only within a sprite map.
 */
public class SpriteMapTileReference implements IAssetReference<Sprite> {
	private static final Logging LOGGER = Logging.createLogger("SpriteMapTileReference", LoggingLevel.DEBUG);

	private final String spriteMapName;
	private final int tileIndex;
	private final String uniqueId;

	/**
	 * Creates a reference to a specific tile within a sprite map.
	 *
	 * @param spriteMapName The name of the sprite map (without namespace)
	 * @param tileIndex The index of the tile within the sprite map
	 */
	public SpriteMapTileReference(String spriteMapName, int tileIndex) {
		this.spriteMapName = spriteMapName;
		this.tileIndex = tileIndex;
		// Create a unique ID for this tile reference
		this.uniqueId = spriteMapName + "_tile_" + tileIndex;
	}

	@Override
	public String getAssetId() {
		return uniqueId;
	}

	@Override
	public Class<Sprite> getAssetType() {
		return Sprite.class;
	}

	/**
	 * Gets the sprite map name (without namespace).
	 */
	public String getSpriteMapName() {
		return spriteMapName;
	}

	/**
	 * Gets the tile index.
	 */
	public int getTileIndex() {
		return tileIndex;
	}

	/**
	 * Resolves this reference to the actual sprite.
	 * This handles the namespacing of sprite map IDs.
	 */
	public Sprite resolveSprite() {
		try {
			// Create a properly namespaced reference to the sprite map
			String namespacedId = AssetReferenceFactory.getNamespacedAssetId(spriteMapName, SpriteMap.class);
			SpriteMapReference spriteMapRef = new SpriteMapReference(namespacedId);

			// Get the sprite map using the namespaced ID
			SpriteMap spriteMap = AssetManager.getInstance().getAsset(spriteMapRef);
			if (spriteMap != null) {
				return spriteMap.getTile(tileIndex);
			}
		} catch (Exception e) {
			LOGGER.error("Error resolving sprite map tile: " + spriteMapName +
				" index: " + tileIndex + " - " + e.getMessage());
		}
		return null;
	}
}