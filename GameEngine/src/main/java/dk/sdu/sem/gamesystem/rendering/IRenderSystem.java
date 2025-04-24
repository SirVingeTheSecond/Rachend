package dk.sdu.sem.gamesystem.rendering;

import dk.sdu.sem.gamesystem.services.ILateUpdate;
import javafx.scene.canvas.GraphicsContext;

public interface IRenderSystem extends ILateUpdate {
	void initialize(GraphicsContext gc);
	void clear();
}