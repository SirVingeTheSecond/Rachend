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
import dk.sdu.sem.gamesystem.rendering.SpriteMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Parser for tile animations from Tiled format.
 * Extracts animation data from the tilesets in room data.
 */
public class TileAnimationParser implements ITileAnimationParser {

	/**
	 * Extracts animation data from a tileset and adds it to an entity.
	 *
	 * @param entity The entity to add animation data to
	 * @param tilemapComponent The tilemap component with tile data
	 * @param roomData The room data containing tileset information
	 * @param tilesetIndex The index of the tileset to extract animations from
	 */
	@Override
	public void parseAndApplyAnimations(Entity entity, TilemapComponent tilemapComponent,
										RoomData roomData, int tilesetIndex) {
		if (tilesetIndex >= roomData.tilesets.size()) {
			return;
		}

		RoomTileset tileset = roomData.tilesets.get(tilesetIndex);
		String tilesetName = tilemapComponent.getTilesetId();

		// Find tiles with animation data
		Map<Integer, List<TileAnimation.Frame>> animationData = extractAnimationData(tileset);

		if (animationData.isEmpty()) {
			return; // No animations found in this tileset
		}

		// Create or get existing animation component
		TileAnimatorComponent animComponent = entity.getComponent(TileAnimatorComponent.class);
		if (animComponent == null) {
			animComponent = new TileAnimatorComponent();
			entity.addComponent(animComponent);
		}

		// For each animated tile, create and register animation
		for (Map.Entry<Integer, List<TileAnimation.Frame>> entry : animationData.entrySet()) {
			int tileId = entry.getKey();
			List<TileAnimation.Frame> frames = entry.getValue();

			// Create frame references and durations
			List<IAssetReference<Sprite>> frameRefs = new ArrayList<>();
			List<Float> frameDurations = new ArrayList<>();

			for (TileAnimation.Frame frame : frames) {
				// Convert to spriteMap tile reference - this gets the sprite from the tileset
				SpriteMap spriteMap = AssetFacade.preloadAsType(tilesetName, SpriteMap.class);

				// Skip if sprite map couldn't be loaded
				if (spriteMap == null) {
					continue;
				}

				// Add frame reference and duration
				IAssetReference<Sprite> spriteRef = AssetFacade.createSpriteMapTileReference(tilesetName, frame.tileId);
				frameRefs.add(spriteRef);
				frameDurations.add(frame.duration / 1000.0f);
			}

			// Create and add animation if we have valid frames
			if (!frameRefs.isEmpty()) {
				TileAnimation animation = new TileAnimation(frameRefs, frameDurations, true);
				animComponent.addTileAnimation(tileId, animation);
				System.out.println("Added animation for tile ID " + tileId + " with " + frameRefs.size() + " frames");
			}
		}
	}

	/**
	 * Extracts animation data from a tileset.
	 *
	 * @param tileset The tileset to extract from
	 * @return Map of tile IDs to their animation frames
	 */
	private static Map<Integer, List<TileAnimation.Frame>> extractAnimationData(RoomTileset tileset) {
		return tileset.tiles.stream()
			.filter(TileAnimationParser::hasAnimation)
			.collect(Collectors.toMap(tile -> tile.id, TileAnimationParser::parseAnimationFrames));
	}

	/**
	 * Checks if a tile has animation properties.
	 */
	private static boolean hasAnimation(RoomTileset.Tile tile) {
		// Check if tile has an "animation" property
		return tile.properties.stream()
			.anyMatch(p -> p.name.equals("animation"));
	}

	/**
	 * Parses animation frames from a tile.
	 */
	private static List<TileAnimation.Frame> parseAnimationFrames(RoomTileset.Tile tile) {
		List<TileAnimation.Frame> frames = new ArrayList<>();

		// Find the animation property
		for (RoomTileset.Tile.Property property : tile.properties) {
			if (property.name.equals("animation")) {
				// Animation data might be stored in various formats
				// depending on Tiled export format
				// For now, we'll assume a simple string format like:
				// "frame1:duration1,frame2:duration2,..."
				if (property.value instanceof String animData) {
					frames.addAll(parseAnimationString(animData));
				}
				break;
			}
		}

		return frames;
	}

	/**
	 * Parses animation data from a string format.
	 */
	private static List<TileAnimation.Frame> parseAnimationString(String animData) {
		List<TileAnimation.Frame> frames = new ArrayList<>();

		// Parse animation data from string
		// Format: "frame1:duration1,frame2:duration2,..."
		String[] frameDefs = animData.split(",");
		for (String frameDef : frameDefs) {
			String[] parts = frameDef.trim().split(":");
			if (parts.length == 2) {
				try {
					int tileId = Integer.parseInt(parts[0]);
					int duration = Integer.parseInt(parts[1]);
					frames.add(new TileAnimation.Frame(tileId, duration));
				} catch (NumberFormatException e) {
					// Skip invalid frame definitions
					System.err.println("Invalid animation frame definition: " + frameDef);
				}
			}
		}

		return frames;
	}
}