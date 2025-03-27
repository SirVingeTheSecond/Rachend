package dk.sdu.sem.gamesystem.rendering;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.data.SpriteNode;
import dk.sdu.sem.gamesystem.data.TilemapNode;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.*;
import java.util.stream.Collectors;

public class FXRenderSystem implements IRenderSystem {

	private static final FXRenderSystem instance = new FXRenderSystem();

	private GraphicsContext gc;

	public static FXRenderSystem getInstance() {
		return instance;
	}

	@Override
	public void initialize(GraphicsContext gc) {
		this.gc = gc;
		if (gc != null) {
			gc.setImageSmoothing(false);
		}
	}

	@Override
	public void lateUpdate() {
		render();
	}

	/**
	 * Renders the current game state.
	 */
	private void render() {
		// Skip rendering if GraphicsContext is not initialized
		if (gc == null) {
			return;
		}

		// Clear the screen
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

		// First render tilemaps (sorted by render layer)
		renderTilemaps();

		// Then render sprites (sorted by render layer and batched by texture)
		renderSpritesWithBatching();
	}

	private void renderTilemaps() {
		List<TilemapNode> sortedTilemaps = NodeManager.active().getNodes(TilemapNode.class).stream()
			.filter(node -> node.tilemap.isVisible())
			.filter(this::isNodeVisible)
			.sorted(Comparator.comparingInt(node -> node.tilemap.getRenderLayer()))
			.toList();

		for (TilemapNode node : sortedTilemaps) {
			renderTilemap(node);
		}
	}

	/**
	 * Checks if a TilemapNode is visible within the viewport.
	 */
	private boolean isNodeVisible(TilemapNode node) {
		Vector2D position = node.transform.getPosition();
		int tileSize = node.tilemap.getTileSize();
		int[][] tileIndices = node.tilemap.getTileIndices();

		if (tileIndices == null || tileIndices.length == 0) {
			return false;
		}

		double mapWidth = tileIndices.length * tileSize;
		double mapHeight = tileIndices[0].length * tileSize;

		// Check if the tilemap intersects with the viewport
		return !(position.x() + mapWidth < 0 ||
			position.x() > gc.getCanvas().getWidth() ||
			position.y() + mapHeight < 0 ||
			position.y() > gc.getCanvas().getHeight());
	}

	private void renderTilemap(TilemapNode node) {
		// Skip if no sprite map or tile indices
		SpriteMap spriteMap = node.tilemap.getSpriteMap();
		if (spriteMap == null || node.tilemap.getTileIndices() == null) {
			return;
		}

		Vector2D position = node.transform.getPosition();
		int tileSize = node.tilemap.getTileSize();
		int[][] tileIndices = node.tilemap.getTileIndices();

		// Calculate view bounds for culling (only render visible tiles)
		double canvasWidth = gc.getCanvas().getWidth();
		double canvasHeight = gc.getCanvas().getHeight();

		int startCol = Math.max(0, (int)(-position.x() / tileSize));
		int endCol = Math.min(tileIndices.length, (int)((-position.x() + canvasWidth) / tileSize) + 1);
		int startRow = Math.max(0, (int)(-position.y() / tileSize));
		int endRow = Math.min(tileIndices[0].length, (int)((-position.y() + canvasHeight) / tileSize) + 1);

		// Create a map to batch tiles by sprite/texture
		Map<Integer, List<TileRenderData>> batchMap = new HashMap<>();

		// Group visible tiles by tile index for batching
		for (int x = startCol; x < endCol; x++) {
			for (int y = startRow; y < endRow; y++) {
				int tileId = tileIndices[x][y];
				if (tileId >= 0) { // Skip negative tile IDs
					double drawX = position.x() + (x * tileSize);
					double drawY = position.y() + (y * tileSize);

					// Add to batch
					batchMap.computeIfAbsent(tileId, k -> new ArrayList<>())
						.add(new TileRenderData(drawX, drawY, tileSize, tileSize));
				}
			}
		}

		// Render each batch
		for (Map.Entry<Integer, List<TileRenderData>> batch : batchMap.entrySet()) {
			int tileId = batch.getKey();
			List<TileRenderData> tiles = batch.getValue();

			// Get the sprite for this tile
			Sprite sprite = spriteMap.getTile(tileId);
			if (sprite != null) {
				// Draw all tiles with the same sprite
				for (TileRenderData tile : tiles) {
					sprite.draw(gc, tile.x, tile.y, tile.width, tile.height);
				}
			}
		}
	}

	/**
	 * Helper class for batched tile rendering data
	 */
	private static class TileRenderData {
		final double x, y, width, height;

		TileRenderData(double x, double y, double width, double height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
	}

	/**
	 * Renders sprites with batching by texture to reduce draw calls
	 */
	private void renderSpritesWithBatching() {
		// Get all visible sprite nodes
		List<SpriteNode> visibleSprites = NodeManager.active().getNodes(SpriteNode.class).stream()
			.filter(node -> node.spriteRenderer.isVisible())
			.filter(this::isNodeVisible)
			.sorted(Comparator.comparingInt(node -> node.spriteRenderer.getRenderLayer()))
			.toList();

		// Update all animations first
		for (SpriteNode node : visibleSprites) {
			updateAnimation(node);
		}

		// Group by image/texture for batching
		Map<Image, List<SpriteNode>> batchGroups = visibleSprites.stream()
			.filter(node -> node.spriteRenderer.getSprite() != null)
			.collect(Collectors.groupingBy(node ->
				node.spriteRenderer.getSprite().getImage()));

		// Render each batch
		for (Map.Entry<Image, List<SpriteNode>> batch : batchGroups.entrySet()) {
			// Sprites with the same texture are rendered together
			for (SpriteNode node : batch.getValue()) {
				renderSprite(node);
			}
		}
	}

	/**
	 * Updates sprite animation if present
	 */
	private void updateAnimation(SpriteNode node) {
		// Get entity from the node
		Entity entity = node.getEntity();

		// Check if entity has an AnimatorComponent
		AnimatorComponent animator = entity.getComponent(AnimatorComponent.class);

		// If entity has an animator, update through that
		if (animator != null) {
			// Ensure the current animation frame is what is shown by the sprite renderer
			SpriteAnimation currentAnimation = animator.getCurrentAnimation();
			if (currentAnimation != null) {
				// Get the current frame reference directly from the animation
				IAssetReference<Sprite> frameReference = currentAnimation.getCurrentFrameReference();
				if (frameReference != null) {
					// Pass the reference to the renderer
					node.spriteRenderer.setSprite(frameReference);
				}
			}
		}
	}

	/**
	 * Checks if a sprite node is visible within the viewport
	 */
	private boolean isNodeVisible(SpriteNode node) {
		if (node.spriteRenderer.getSprite() == null) {
			return false;
		}

		Vector2D position = node.transform.getPosition();
		Vector2D scale = node.transform.getScale();

		double width = node.spriteRenderer.getSprite().getSourceRect().getWidth() * scale.x();
		double height = node.spriteRenderer.getSprite().getSourceRect().getHeight() * scale.y();

		// Calculate sprite boundaries (centered)
		double left = position.x() - (width / 2);
		double right = position.x() + (width / 2);
		double top = position.y() - (height / 2);
		double bottom = position.y() + (height / 2);

		// Check if sprite is within viewport bounds
		return !(right < 0 ||
			left > gc.getCanvas().getWidth() ||
			bottom < 0 ||
			top > gc.getCanvas().getHeight());
	}

	// Update to renderSprite method in FXRenderSystem.java
	private void renderSprite(SpriteNode node) {
		SpriteRendererComponent renderer = node.spriteRenderer;

		// Get sprite through reference resolution
		Sprite sprite = renderer.getSprite();

		// Skip if no sprite
		if (sprite == null) {
			return;
		}

		Vector2D position = node.transform.getPosition();
		Vector2D scale = node.transform.getScale();

		// Calculate sprite dimensions based on scale
		double width = sprite.getSourceRect().getWidth() * scale.x();
		double height = sprite.getSourceRect().getHeight() * scale.y();

		// Calculate sprite position (centered on transform position)
		double x = position.x() - (width / 2);
		double y = position.y() - (height / 2);

		// Draw the sprite
		sprite.draw(
			gc, x, y, width, height,
			renderer.isFlipX(), renderer.isFlipY()
		);
	}
}