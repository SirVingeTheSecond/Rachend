package dk.sdu.sem.gamesystem.animation;

import dk.sdu.sem.commonlevel.ITileAnimationParser;
import dk.sdu.sem.commonlevel.room.RoomData;
import dk.sdu.sem.commonlevel.room.RoomTileset;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commontilemap.TilemapComponent;
import dk.sdu.sem.gamesystem.animation.utils.TileIndexConverter;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.components.TileAnimatorComponent;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

import java.util.*;

/**
 * Extracts animation information from a Tiled tileset and attaches a
 * TileAnimatorComponent to the entity that owns the TilemapComponent.
 * <p>
 * The parser and the renderer work with 0-based local tile‑IDs,
 * so we convert everything up-front and cache the result to avoid
 * parsing the same tileset multiple times.
 */
public class TileAnimationParser implements ITileAnimationParser {
	private static final Logging LOGGER = Logging.createLogger("TileAnimationParser", LoggingLevel.DEBUG);

	// Could be cool to standardize this
	private static final String LOG_TAG = "TileAnimationParser";

	// Cache of animations by tileset name
	private static final Map<String, Map<Integer, TileAnimation>> CACHE = new HashMap<>();

	@Override
	public void parseAndApplyAnimations(Entity entity, TilemapComponent tilemap, RoomData roomData, int tilesetIndex) {
		// Validate input parameters
		if (entity == null || tilemap == null || roomData == null) {
			System.err.println(LOG_TAG + ": Invalid parameters for parseAndApplyAnimations");
			return;
		}

		if (tilesetIndex < 0 || tilesetIndex >= roomData.tilesets.size()) {
			return;
		}

		RoomTileset tileset = roomData.tilesets.get(tilesetIndex);
		String sheetName = tilemap.getTilesetId();

		if (sheetName == null || sheetName.isEmpty()) {
			LOGGER.error(LOG_TAG + ": Invalid tileset ID");
			return;
		}

		// Get or build animations for this tileset
		Map<Integer, TileAnimation> animations = getOrBuildAnimations(tileset, sheetName, roomData, tilesetIndex);

		if (animations.isEmpty()) {
			LOGGER.debug("No animations found for tileset: " + sheetName);
			return;
		}

		// Add animations to the entity
		addAnimationsToEntity(entity, animations);
	}

	/**
	 * Gets or creates a TileAnimatorComponent for the entity and adds animations to it.
	 */
	private void addAnimationsToEntity(Entity entity, Map<Integer, TileAnimation> animations) {
		// Get or create the animation component
		TileAnimatorComponent animComp = entity.getComponent(TileAnimatorComponent.class);
		if (animComp == null) {
			animComp = new TileAnimatorComponent();
			entity.addComponent(animComp);
		}

		// Add all animations to the component
		animations.forEach(animComp::addTileAnimation);
	}

	/**
	 * Gets cached animations or builds new ones for the tileset.
	 */
	private Map<Integer, TileAnimation> getOrBuildAnimations(
		RoomTileset tileset, String sheetName, RoomData roomData, int tilesetIndex) {

		return CACHE.computeIfAbsent(sheetName, key -> {
			int firstGid = TileIndexConverter.getFirstGid(roomData, tilesetIndex);
			return buildAnimations(tileset, sheetName, firstGid);
		});
	}

	/**
	 * Builds all animations for a tileset and returns them.
	 */
	private static Map<Integer, TileAnimation> buildAnimations(RoomTileset tileset, String sheetName, int firstGid) {
		// Extract raw animation data from tileset
		Map<Integer, List<TileAnimation.Frame>> rawAnimationData = extractAnimationData(tileset);
		LOGGER.debug("Raw animation data for " + sheetName + ": " + rawAnimationData);

		// Convert raw data to TileAnimation objects
		Map<Integer, TileAnimation> result = new HashMap<>();

		for (Map.Entry<Integer, List<TileAnimation.Frame>> entry : rawAnimationData.entrySet()) {
			int localId = entry.getKey();
			List<TileAnimation.Frame> frames = entry.getValue();

			LOGGER.debug("Processing animation for tile " + localId);

			// Create animation references and durations
			List<IAssetReference<Sprite>> frameRefs = new ArrayList<>();
			List<Float> durations = new ArrayList<>();

			for (TileAnimation.Frame frame : frames) {
				// Convert to local (0-based) tile ID
				int localTileId = TileIndexConverter.globalToLocal(frame.tileId, firstGid);

				// Create sprite reference and add duration
				frameRefs.add(AssetFacade.createSpriteMapTileReference(sheetName, localTileId));
				durations.add(frame.duration / 1000f); // Convert ms to seconds

				LOGGER.debug("Added frame: tileId=" + localTileId + " duration=" + frame.duration);
			}

			if (!frameRefs.isEmpty()) {
				result.put(localId, new TileAnimation(frameRefs, durations, true));
			}
		}

		return result;
	}

	/**
	 * Extracts animation data from a tileset.
	 */
	private static Map<Integer, List<TileAnimation.Frame>> extractAnimationData(RoomTileset tileset) {
		Map<Integer, List<TileAnimation.Frame>> animationMap = new HashMap<>();

		if (tileset == null || tileset.tiles == null) {
			return animationMap;
		}

		for (RoomTileset.Tile tile : tileset.tiles) {
			if (tile.animation == null || tile.animation.isEmpty()) {
				continue;
			}

			List<TileAnimation.Frame> frames = new ArrayList<>();

			// Convert each animation frame
			tile.animation.forEach(frame ->
				frames.add(new TileAnimation.Frame(frame.tileId, frame.duration))
			);

			animationMap.put(tile.id, frames);
		}

		return animationMap;
	}
}