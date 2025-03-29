package dk.sdu.sem.uisystem;

import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;

public class Crosshair implements IGUIUpdate {
	@Override
	public void onGUI(GraphicsContext gc) {

		Vector2D point = Input.getMousePosition();
		
		gc.setGlobalBlendMode(BlendMode.DIFFERENCE);
		gc.setFill(Color.WHITE);
		gc.fillRect(point.x() - 10, point.y() - 1, 8, 2);
		gc.fillRect(point.x() - 1,  point.y() - 10, 2, 8);

		gc.fillRect(point.x() + 2, point.y() - 1, 8, 2);
		gc.fillRect(point.x() - 1,  point.y() + 2, 2, 8);
		gc.setGlobalBlendMode(BlendMode.SRC_OVER);
	}
}
