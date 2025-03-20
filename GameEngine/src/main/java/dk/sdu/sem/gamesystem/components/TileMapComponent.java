package dk.sdu.sem.gamesystem.components;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.gamesystem.rendering.TileSet;

public class TileMapComponent implements IComponent {
	private TileSet tileSet;
	private int[][] tileIndices;
	private int tileSize;
	private int renderLayer = 0;
	private boolean isVisible = true;

	public TileMapComponent() {
	}

	public TileMapComponent(TileSet tileSet, int[][] tileIndices, int tileSize) {
		this.tileSet = tileSet;
		this.tileIndices = tileIndices;
		this.tileSize = tileSize;
	}

	public TileSet getTileSet() {
		return tileSet;
	}

	public void setTileSet(TileSet tileSet) {
		this.tileSet = tileSet;
	}

	public int[][] getTileIndices() {
		return tileIndices;
	}

	public void setTileIndices(int[][] tileIndices) {
		this.tileIndices = tileIndices;
	}

	public int getTileSize() {
		return tileSize;
	}

	public void setTileSize(int tileSize) {
		this.tileSize = tileSize;
	}

	public int getRenderLayer() {
		return renderLayer;
	}

	public void setRenderLayer(int renderLayer) {
		this.renderLayer = renderLayer;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean visible) {
		this.isVisible = visible;
	}
	
	public void setTile(int x, int y, int tileId) {
		if (x >= 0 && x < tileIndices.length && y >= 0 && y < tileIndices[0].length) {
			tileIndices[x][y] = tileId;
		}
	}

	public int getTile(int x, int y) {
		if (x >= 0 && x < tileIndices.length && y >= 0 && y < tileIndices[0].length) {
			return tileIndices[x][y];
		}
		return -1; // Invalid tile
	}
}
