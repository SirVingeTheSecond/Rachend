package dk.sdu.sem.gamesystem.animation;

import dk.sdu.sem.commonlevel.ITileAnimationParser;
import dk.sdu.sem.commonlevel.room.RoomData;
import dk.sdu.sem.commonlevel.room.RoomTileset;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commontilemap.TilemapComponent;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.components.TileAnimatorComponent;
import dk.sdu.sem.gamesystem.rendering.Sprite;

import java.util.*;

/**
 * Extracts <animation> information from a Tiled tileset and attaches a
 * TileAnimatorComponent to the entity that owns the TilemapComponent.
 *
 * The parser and the renderer both work exclusively with **0‑based local
 * tile‑IDs**, so we convert everything up‑front and cache the result to avoid
 * parsing the same tileset over and over again.
 */
public class TileAnimationParser implements ITileAnimationParser {

	/** one cache entry per tileset (key = tileset image name) */
	private static final Map<String, Map<Integer, TileAnimation>> CACHE = new HashMap<>();

	@Override
	public void parseAndApplyAnimations(Entity entity, TilemapComponent tilemap, RoomData roomData, int tilesetIndex) {

		if (tilesetIndex < 0 || tilesetIndex >= roomData.tilesets.size()) {
			return;
		}

		RoomTileset tileset = roomData.tilesets.get(tilesetIndex);
		String sheetName = tilemap.getTilesetId();

		// build the prepared animation map for this tileset
		Map<Integer, TileAnimation> animations =
			CACHE.computeIfAbsent(sheetName, key -> buildAnimations(tileset, sheetName));

		if (animations.isEmpty()) return;

		// attach/merge into the entity’s TileAnimatorComponent
		TileAnimatorComponent animComp = entity.getComponent(TileAnimatorComponent.class);
		if (animComp == null) {
			animComp = new TileAnimatorComponent();
			entity.addComponent(animComp);
		}
		animations.forEach(animComp::addTileAnimation);
	}

	/** Builds (once) all animations for a tileset and caches them */
	private static Map<Integer, TileAnimation> buildAnimations(RoomTileset tileset,
															   String sheetName) {

		Map<Integer, List<TileAnimation.Frame>> raw = extractAnimationData(tileset);

		Map<Integer, TileAnimation> result = new HashMap<>();

		raw.forEach((localId, frames) -> {

			List<IAssetReference<Sprite>> frameRefs   = new ArrayList<>();
			List<Float>                   durations   = new ArrayList<>();

			for (TileAnimation.Frame f : frames) {
				frameRefs.add(AssetFacade.createSpriteMapTileReference(sheetName, f.tileId));
				durations.add(f.duration / 1000f);
			}

			if (!frameRefs.isEmpty()) {
				result.put(localId, new TileAnimation(frameRefs, durations, true));
			}
		});

		return result;
	}

	private static Map<Integer, List<TileAnimation.Frame>> extractAnimationData(RoomTileset tileset) {

		Map<Integer, List<TileAnimation.Frame>> map = new HashMap<>();

		for (RoomTileset.Tile tile : tileset.tiles) {
			if (tile.animation == null || tile.animation.isEmpty()) continue;

			List<TileAnimation.Frame> frames = new ArrayList<>();

			tile.animation.forEach(frame ->
				frames.add(new TileAnimation.Frame(frame.tileId, frame.duration))
			);

			map.put(tile.id, frames);
		}
		return map;
	}
}