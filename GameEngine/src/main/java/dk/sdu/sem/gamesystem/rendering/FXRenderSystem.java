package dk.sdu.sem.gamesystem.rendering;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.data.SpriteNode;
import dk.sdu.sem.gamesystem.data.TileMapNode;
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
		renderTileMaps();

		// Then render sprites (sorted by render layer and batched by texture)
		renderSpritesWithBatching();
	}

	private void renderTileMaps() {
		List<TileMapNode> sortedTileMaps = NodeManager.active().getNodes(TileMapNode.class).stream()
			.filter(node -> node.tileMap.isVisible())
			.filter(node -> isNodeVisible(node))
			.sorted(Comparator.comparingInt(node -> node.tileMap.getRenderLayer()))
			.toList();

		for (TileMapNode node : sortedTileMaps) {
			renderTileMap(node);
		}
	}

	/**
	 * Checks if a TileMapNode is visible within the viewport.
	 */
	private boolean isNodeVisible(TileMapNode node) {
		Vector2D position = node.transform.getPosition();
		int tileSize = node.tileMap.getTileSize();
		int[][] tileIndices = node.tileMap.getTileIndices();

		if (tileIndices == null || tileIndices.length == 0) {
			return false;
		}

		double mapWidth = tileIndices.length * tileSize;
		double mapHeight = tileIndices[0].length * tileSize;

		// Check if the tilemap intersects with the viewport
		return !(position.getX() + mapWidth < 0 ||
			position.getX() > gc.getCanvas().getWidth() ||
			position.getY() + mapHeight < 0 ||
			position.getY() > gc.getCanvas().getHeight());
	}

	private void renderTileMap(TileMapNode node) {
		// Skip if no tileset or tile indices
		if (node.tileMap.getTileSet() == null || node.tileMap.getTileIndices() == null) {
			return;
		}

		Vector2D position = node.transform.getPosition();
		int tileSize = node.tileMap.getTileSize();
		int[][] tileIndices = node.tileMap.getTileIndices();

		// Calculate view bounds for culling (only render visible tiles)
		double canvasWidth = gc.getCanvas().getWidth();
		double canvasHeight = gc.getCanvas().getHeight();

		int startCol = Math.max(0, (int)(-position.getX() / tileSize));
		int endCol = Math.min(tileIndices.length, (int)((-position.getX() + canvasWidth) / tileSize) + 1);
		int startRow = Math.max(0, (int)(-position.getY() / tileSize));
		int endRow = Math.min(tileIndices[0].length, (int)((-position.getY() + canvasHeight) / tileSize) + 1);

		// Create a map to batch tiles by sprite/texture
		Map<Sprite, List<TileRenderData>> batchMap = new HashMap<>();

		// Group visible tiles by sprite for batching
		for (int x = startCol; x < endCol; x++) {
			for (int y = startRow; y < endRow; y++) {
				int tileId = tileIndices[x][y];
				if (tileId >= 0) { // Skip negative tile IDs
					Sprite tileSprite = node.tileMap.getTileSet().getTileSprite(tileId);
					if (tileSprite != null) {
						double drawX = position.getX() + (x * tileSize);
						double drawY = position.getY() + (y * tileSize);

						// Add to batch
						batchMap.computeIfAbsent(tileSprite, k -> new ArrayList<>())
							.add(new TileRenderData(drawX, drawY, tileSize, tileSize));
					}
				}
			}
		}

		// Render each batch
		for (Map.Entry<Sprite, List<TileRenderData>> batch : batchMap.entrySet()) {
			Sprite sprite = batch.getKey();
			List<TileRenderData> tiles = batch.getValue();

			// Draw all tiles with the same sprite
			for (TileRenderData tile : tiles) {
				sprite.draw(gc, tile.x, tile.y, tile.width, tile.height);
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
			.collect(Collectors.toList());

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
		SpriteRendererComponent renderer = node.spriteRenderer;

		// Update animation if present
		if (renderer.getCurrentAnimation() != null) {
			renderer.getCurrentAnimation().update(Time.getDeltaTime());
			renderer.setSprite(renderer.getCurrentAnimation().getCurrentFrame());
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

		double width = node.spriteRenderer.getSprite().getSourceRect().getWidth() * scale.getX();
		double height = node.spriteRenderer.getSprite().getSourceRect().getHeight() * scale.getY();

		// Calculate sprite boundaries (center-based positioning)
		double left = position.getX() - (width / 2);
		double right = position.getX() + (width / 2);
		double top = position.getY() - (height / 2);
		double bottom = position.getY() + (height / 2);

		// Check if sprite is within viewport bounds
		return !(right < 0 ||
			left > gc.getCanvas().getWidth() ||
			bottom < 0 ||
			top > gc.getCanvas().getHeight());
	}

	private void renderSprite(SpriteNode node) {
		SpriteRendererComponent renderer = node.spriteRenderer;

		// Skip if no sprite
		if (renderer.getSprite() == null) {
			return;
		}

		Vector2D position = node.transform.getPosition();
		Vector2D scale = node.transform.getScale();

		// Calculate sprite dimensions based on scale
		double width = renderer.getSprite().getSourceRect().getWidth() * scale.getX();
		double height = renderer.getSprite().getSourceRect().getHeight() * scale.getY();

		// Calculate sprite position (centered on transform position)
		double x = position.getX() - (width / 2);
		double y = position.getY() - (height / 2);

		// Draw the sprite
		renderer.getSprite().draw(
			gc, x, y, width, height,
			renderer.isFlipX(), renderer.isFlipY()
		);
	}
}