package dk.sdu.sem.gamesystem.rendering;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commontilemap.TileAnimationComponent;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.data.SpriteNode;
import dk.sdu.sem.gamesystem.data.TilemapNode;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.stream.Collectors;

public class FXRenderSystem implements IRenderSystem {

	private static final FXRenderSystem instance = new FXRenderSystem();

	private GraphicsContext gc;
	private  Canvas canvas;
	private final HashMap<TilemapNode, WritableImage> snapshots = new HashMap<>();

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
	public void clear() {
		// Original clear method logic
		snapshots.clear();

		// Invalidate all tilemap snapshots by updating TilemapRendererComponents
		Set<TilemapNode> tilemapNodes = NodeManager.active().getNodes(TilemapNode.class);
		for (TilemapNode node : tilemapNodes) {
			node.renderer.invalidateSnapshot();
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

		renderAllObjectsSorted();
	}

	/**
	 * Renders all objects with respect to layer order and Y-position sorting
	 */
	private void renderAllObjectsSorted() {
		try {
			// Get all visible tilemap and sprite nodes
			List<TilemapNode> tilemapNodes = NodeManager.active().getNodes(TilemapNode.class).stream()
				.filter(node -> node.tilemap.isVisible())
				.filter(this::isNodeVisible)
				.toList();

			List<SpriteNode> spriteNodes = NodeManager.active().getNodes(SpriteNode.class).stream()
				.filter(node -> node.spriteRenderer.isVisible())
				.filter(this::isNodeVisible)
				.toList();

			// Combined list of all renderables
			List<RenderableItem> renderables = new ArrayList<>();

			// Adding the renderables to the list
			for (TilemapNode node : tilemapNodes) {
				renderables.add(new RenderableItem(node, RenderableType.TILEMAP, node.renderer.getRenderLayer()));
			}
			for (SpriteNode node : spriteNodes) {
				renderables.add(new RenderableItem(node, RenderableType.SPRITE, node.spriteRenderer.getRenderLayer()));
			}

			// Sort all renderables by layer first, then Y position
			renderables.sort(Comparator
				.comparingInt(RenderableItem::getRenderLayer)
				.thenComparingDouble(RenderableItem::getYPosition));

			// Render each item in sorted order
			for (RenderableItem item : renderables) {
				if (item.type == RenderableType.TILEMAP) {
					renderTilemap((TilemapNode)item.node);
				} else {
					renderSprite((SpriteNode)item.node);
				}
			}

		} catch (Exception e) {
			System.err.println("Error in renderAllObjectsSorted: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Renders a single tilemap.
	 */
	private void renderTilemap(TilemapNode node) {
		// Check if we have a valid snapshot that hasn't been invalidated
		if (snapshots.containsKey(node) && node.renderer.isSnapshotValid()) {
			gc.drawImage(snapshots.get(node), 0, 0);
			return;
		}

		// Skip if no sprite map or tile indices
		SpriteMap spriteMap = node.renderer.getSpriteMap();
		if (spriteMap == null || node.tilemap.getTileIndices() == null) {
			return;
		}

		// Get animation component if available
		TileAnimationComponent animComponent = node.getEntity().getComponent(TileAnimationComponent.class);

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

		if (canvas == null) {
			canvas = new Canvas(canvasWidth, canvasHeight);
			canvas.getGraphicsContext2D().setImageSmoothing(false);
		}

		canvas.getGraphicsContext2D().clearRect(0, 0, canvasWidth, canvasHeight);

		for (int x = startCol; x < endCol; x++) {
			for (int y = startRow; y < endRow; y++) {
				int tileId = tileIndices[x][y];
				if (tileId >= 0) { // Skip negative tile IDs
					double drawX = position.x() + (x * tileSize);
					double drawY = position.y() + (y * tileSize);

					// Get sprite, checking for animations
					Sprite sprite;
					if (animComponent != null && animComponent.hasTileAnimation(tileId)) {
						sprite = animComponent.getCurrentFrameSprite(tileId);
					} else {
						sprite = spriteMap.getTile(tileId);
					}

					if (sprite != null) {
						sprite.draw(canvas.getGraphicsContext2D(), drawX, drawY, tileSize, tileSize, 0);
					}
				}
			}
		}

		SnapshotParameters sp = new SnapshotParameters();
		sp.setFill(Color.TRANSPARENT);

		WritableImage snapshot = canvas.snapshot(sp, null);
		if (!snapshots.containsKey(node)) {
			snapshots.put(node, snapshot);
			gc.drawImage(snapshot, 0, 0);
		}

		// Mark snapshot as valid after drawing
		node.renderer.markSnapshotValid();
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

	/**
	 * Updates sprite animation if present
	 */
	private void updateAnimation(SpriteNode node) {
		Entity entity = node.getEntity();
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
	 * Renders a single sprite
	 */
	private void renderSprite(SpriteNode node) {
		SpriteRendererComponent renderer = node.spriteRenderer;

		// Update animation before rendering
		updateAnimation(node);

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
			gc, x, y, width, height, node.transform.getRotation(),
			renderer.isFlipX(), renderer.isFlipY()
		);
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

	private enum RenderableType {
		TILEMAP,
		SPRITE
	}

	// Information for depth sorting
	private static class RenderableItem {
		final Node node;
		final RenderableType type;
		final int renderLayer;
		final float yPosition;

		RenderableItem(Node node, RenderableType type, int renderLayer) {
			this.node = node;
			this.type = type;
			this.renderLayer = renderLayer;

			// Get y position based on node type
			if (type == RenderableType.TILEMAP) {
				this.yPosition = ((TilemapNode)node).transform.getPosition().y();
			} else {
				this.yPosition = ((SpriteNode)node).transform.getPosition().y();
			}
		}

		int getRenderLayer() {
			return renderLayer;
		}

		float getYPosition() {
			return yPosition;
		}
	}
}