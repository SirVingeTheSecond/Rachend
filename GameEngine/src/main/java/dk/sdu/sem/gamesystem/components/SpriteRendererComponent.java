package dk.sdu.sem.gamesystem.components;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.IShowVisualState;

public class SpriteRendererComponent implements IComponent, IShowVisualState {
	private String visualState;
	@Override
	public String getVisualState() {
		return visualState;
	}
}
