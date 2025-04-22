package dk.sdu.sem.gamesystem.components;

import dk.sdu.sem.commonsystem.IComponent;


/**
 * Simulated light effect on entity sprite
 */
public class PointLightComponent implements IComponent {
	private float size;
	private int r;
	private int g;
	private int b;
	private float brightness;
	private boolean on;
	private int renderLayer;

	public PointLightComponent(float size, int r, int g, int b, float brightness, boolean on, int renderLayer) {
		this.size = size;
		this.r = r;
		this.g = g;
		this.b = b;
		this.brightness = brightness;
		this.on = on;
		this.renderLayer = renderLayer;
	}

	public float getSize() {
		return size;
	}

	public void setSize(float size) {
		this.size = size;
	}

	public int getR() {
		return r;
	}

	public void setR(int r) {
		this.r = r;
	}

	public int getG() {
		return g;
	}

	public void setG(int g) {
		this.g = g;
	}

	public int getB() {
		return b;
	}

	public void setB(int b) {
		this.b = b;
	}

	public float getBrightness() {
		return brightness;
	}

	public void setBrightness(float brightness) {
		this.brightness = brightness;
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	}

	public int getRenderLayer() {
		return renderLayer;
	}

	public void setRenderLayer(int renderLayer) {
		this.renderLayer = renderLayer;
	}
}
