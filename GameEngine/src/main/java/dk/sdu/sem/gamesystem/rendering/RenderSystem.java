package dk.sdu.sem.gamesystem.rendering;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.data.SpriteNode;
import dk.sdu.sem.gamesystem.data.TileMapNode;
import dk.sdu.sem.gamesystem.services.ILateUpdate;
import javafx.scene.canvas.GraphicsContext;

import java.util.Comparator;
import java.util.List;

public class RenderSystem implements ILateUpdate {
	private final GraphicsContext gc;

	public RenderSystem(GraphicsContext gc) {
		this.gc = gc;
		gc.setImageSmoothing(false);
	}

	/**
	 * Renders the current game state.
	 */
	public void render() {
		// Clear the screen
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

		// First render tilemaps (sorted by render layer)
		renderTileMaps();

		// Then render sprites (sorted by render layer)
		renderSprites();
	}

	@Override
	public void lateUpdate() {
		render();
	}

	private void renderTileMaps() {
		List<TileMapNode> sortedTileMaps = NodeManager.active().getNodes(TileMapNode.class).stream()
			.filter(node -> node.tileMap.isVisible())
			.sorted(Comparator.comparingInt(node -> node.tileMap.getRenderLayer()))
			.toList();

		for (TileMapNode node : sortedTileMaps) {
			renderTileMap(node);
		}
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

		// Render visible tiles
		for (int x = startCol; x < endCol; x++) {
			for (int y = startRow; y < endRow; y++) {
				int tileId = tileIndices[x][y];
				if (tileId >= 0) { // Skip negative tile IDs (often used for "no tile")
					Sprite tileSprite = node.tileMap.getTileSet().getTileSprite(tileId);
					if (tileSprite != null) {
						double drawX = position.getX() + (x * tileSize);
						double drawY = position.getY() + (y * tileSize);
						tileSprite.draw(gc, drawX, drawY, tileSize, tileSize);
					}
				}
			}
		}
	}

	private void renderSprites() {
		List<SpriteNode> sortedSprites = NodeManager.active().getNodes(SpriteNode.class).stream()
			.filter(node -> node.spriteRenderer.isVisible())
			.sorted(Comparator.comparingInt(node -> node.spriteRenderer.getRenderLayer()))
			.toList();

		for (SpriteNode node : sortedSprites) {
			renderSprite(node);
		}
	}

	private void renderSprite(SpriteNode node) {
		SpriteRendererComponent renderer = node.spriteRenderer;

		// Update animation if present
		if (renderer.getCurrentAnimation() != null) {
			renderer.getCurrentAnimation().update(Time.getDeltaTime());
			renderer.setSprite(renderer.getCurrentAnimation().getCurrentFrame());
		}

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