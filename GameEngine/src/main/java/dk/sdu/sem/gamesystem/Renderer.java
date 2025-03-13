package dk.sdu.sem.gamesystem;

import javafx.scene.canvas.GraphicsContext;

public class Renderer {
	private final GraphicsContext gc;

	public Renderer(GraphicsContext gc) {
		this.gc = gc;
	}

	public void render() {
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
	}
}
